package com.zhou6.cloud.file.config;

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
 * file-service 当前登录用户上下文拦截器。
 * <p>
 * 从 Authorization 中解析 JWT，并结合 Redis 登录会话写入 UserContextHolder，
 * 供 MyBatis 审计字段自动填充 createBy/updateBy。
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
            if (session != null && claims.getSessionId().equals(session.getSessionId())) {
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
        if (session.getUserId() != null && !session.getUserId().isBlank()) {
            UserContextHolder.setCurrentUser(new CurrentLoginUser(
                    Long.valueOf(session.getUserId()),
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
}
