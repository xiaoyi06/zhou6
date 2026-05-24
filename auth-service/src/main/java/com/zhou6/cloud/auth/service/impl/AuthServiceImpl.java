package com.zhou6.cloud.auth.service.impl;

import java.time.Duration;
import java.util.UUID;

import com.zhou6.cloud.auth.client.UserClient;
import com.zhou6.cloud.auth.dto.LoginRequest;
import com.zhou6.cloud.auth.dto.RefreshRequest;
import com.zhou6.cloud.auth.dto.TokenResponse;
import com.zhou6.cloud.auth.dto.VerifyResponse;
import com.zhou6.cloud.auth.service.AuthService;
import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.common.security.JwtTokenSupport;
import com.zhou6.cloud.common.security.LoginSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final long ACCESS_TOKEN_SECONDS = 15;
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofMinutes(20);

    private final UserClient userClient;
    private final StringRedisTemplate redisTemplate;
    private final JwtTokenSupport jwtSupport;
    private final ObjectMapper objectMapper;

    public AuthServiceImpl(UserClient userClient, StringRedisTemplate redisTemplate, ObjectMapper objectMapper,
            @Value("${jwt.secret}") String jwtSecret) {
        this.userClient = userClient;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.jwtSupport = new JwtTokenSupport(jwtSecret);
    }

    @Override
    public TokenResponse login(LoginRequest request, String clientIp) {
        // 登录时只负责发证，账号密码的具体校验交给 user-service 内部接口。
        R<VerifyResponse> response = userClient.verify(request.getUsername(), request.getPassword());
        VerifyResponse verifyResponse = response.getData();
        if (!response.success() || verifyResponse == null || !verifyResponse.isVerified()) {
            throw new BizException(CommonErrorCode.LOGIN_FAILED);
        }
        return issueTokens(verifyResponse, clientIp);
    }

    @Override
    public TokenResponse refresh(RefreshRequest request, String clientIp) {
        String refreshToken = request.getRefreshToken();
        // refreshToken 只保存随机值，真实用户身份和会话号从 Redis 中读取。
        LoginSession sessionUser = readSessionUser(redisTemplate.opsForValue().get(refreshKey(refreshToken)));
        if (sessionUser == null) {
            throw new BizException(CommonErrorCode.REFRESH_TOKEN_INVALID);
        }
        if (!isCurrentSession(sessionUser.getUserId(), sessionUser.getSessionId(), refreshToken)) {
            throw new BizException(CommonErrorCode.LOGIN_SESSION_EXPIRED);
        }
        // 大令牌一次性使用，刷新成功后立即删除旧值，并签发新会话。
        redisTemplate.delete(refreshKey(refreshToken));
        return issueTokens(toVerifyResponse(sessionUser), clientIp);
    }

    private TokenResponse issueTokens(VerifyResponse user, String clientIp) {
        String userId = user.getUserId();
        String oldRefreshToken = redisTemplate.opsForValue().get(currentRefreshKey(userId));
        if (oldRefreshToken != null && !oldRefreshToken.isBlank()) {
            // 新登录或刷新成功后，删除旧 refreshToken，旧客户端无法继续续期。
            redisTemplate.delete(refreshKey(oldRefreshToken));
        }

        String sessionId = UUID.randomUUID().toString().replace("-", "");
        // accessToken 保留 15 秒有效期，并携带 sessionId，网关据此判断旧登录是否已被踢下线。
        String accessToken = jwtSupport.createToken(userId, sessionId, ACCESS_TOKEN_SECONDS);
        String refreshToken = UUID.randomUUID().toString().replace("-", "");

        // Redis 保存当前有效会话；同一用户新登录会覆盖这里的 sessionId，从而让旧 accessToken 立刻失效。
        String sessionJson = writeSessionUser(user, sessionId);
        redisTemplate.opsForValue().set(refreshKey(refreshToken), sessionJson, REFRESH_TOKEN_TTL);
        redisTemplate.opsForValue().set(currentSessionKey(userId), sessionJson, REFRESH_TOKEN_TTL);
        redisTemplate.opsForValue().set(currentRefreshKey(userId), refreshToken, REFRESH_TOKEN_TTL);
        redisTemplate.opsForValue().set(loginIpKey(userId), clientIp, REFRESH_TOKEN_TTL);
        return new TokenResponse(accessToken, refreshToken, ACCESS_TOKEN_SECONDS);
    }

    private boolean isCurrentSession(String userId, String sessionId, String refreshToken) {
        LoginSession currentSession = readSessionUser(redisTemplate.opsForValue().get(currentSessionKey(userId)));
        String currentRefreshToken = redisTemplate.opsForValue().get(currentRefreshKey(userId));
        return currentSession != null && sessionId.equals(currentSession.getSessionId())
                && refreshToken.equals(currentRefreshToken);
    }

    private String refreshKey(String refreshToken) {
        return "zhou6:auth:refresh:" + refreshToken;
    }

    private String currentSessionKey(String userId) {
        return "zhou6:auth:session:" + userId;
    }

    private String currentRefreshKey(String userId) {
        return "zhou6:auth:current-refresh:" + userId;
    }

    private String loginIpKey(String userId) {
        return "zhou6:auth:login-ip:" + userId;
    }

    private String writeSessionUser(VerifyResponse user, String sessionId) {
        try {
            LoginSession loginSession = new LoginSession();
            loginSession.setUserId(user.getUserId());
            loginSession.setSessionId(sessionId);
            loginSession.setUsername(user.getUsername());
            loginSession.setNickname(user.getNickname());
            loginSession.setEmail(user.getEmail());
            loginSession.setContactPhone(user.getContactPhone());
            return objectMapper.writeValueAsString(loginSession);
        } catch (Exception ex) {
            throw new IllegalStateException("写入登录用户快照失败", ex);
        }
    }

    private LoginSession readSessionUser(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, LoginSession.class);
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.REFRESH_TOKEN_DATA_INVALID);
        }
    }

    private VerifyResponse toVerifyResponse(LoginSession sessionUser) {
        return new VerifyResponse(true, sessionUser.getUserId(), sessionUser.getUsername(),
                sessionUser.getNickname(), sessionUser.getEmail(), sessionUser.getContactPhone());
    }
}
