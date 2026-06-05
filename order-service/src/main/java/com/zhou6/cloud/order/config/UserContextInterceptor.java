package com.zhou6.cloud.order.config;

import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhou6.cloud.common.context.CurrentLoginUser;
import com.zhou6.cloud.common.context.UserContextHolder;
import com.zhou6.cloud.common.security.JwtClaims;
import com.zhou6.cloud.common.security.JwtTokenSupport;
import com.zhou6.cloud.common.security.LoginSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 订单服务用户上下文拦截器，统一从 JWT 和 Redis 会话快照恢复当前操作人。
 */
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private final JwtTokenSupport jwtTokenSupport;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public UserContextInterceptor(@Value("${jwt.secret}") String jwtSecret,
            StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.jwtTokenSupport = new JwtTokenSupport(jwtSecret);
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = resolveToken(request);
        if (token != null) {
            JwtClaims claims = jwtTokenSupport.verifyAndGetClaims(token);
            LoginSession session = readLoginSession(claims.getUserId());
            if (session != null && Objects.equals(claims.getSessionId(), session.getSessionId())) {
                writeCurrentUser(session);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }

    private void writeCurrentUser(LoginSession session) {
        Long userId = parseUserId(session.getUserId());
        if (userId != null) {
            UserContextHolder.setCurrentUser(new CurrentLoginUser(
                    userId,
                    session.getUsername(),
                    session.getNickname(),
                    session.getEmail(),
                    session.getContactPhone()));
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            return null;
        }
        if (authorization.startsWith("Bearer_")) {
            return authorization.substring("Bearer_".length());
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length());
        }
        return null;
    }

    private LoginSession readLoginSession(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        String value = redisTemplate.opsForValue().get("zhou6:auth:session:" + userId);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, LoginSession.class);
        } catch (Exception ex) {
            return null;
        }
    }

    private Long parseUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
