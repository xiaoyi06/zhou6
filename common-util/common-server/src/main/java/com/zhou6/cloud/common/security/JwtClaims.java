package com.zhou6.cloud.common.security;

/**
 * JWT 解析后的核心声明。
 */
public class JwtClaims {

    /** 当前登录用户 ID。 */
    private final String userId;

    /** 当前登录会话 ID，用于网关校验是否仍是 Redis 中的有效会话。 */
    private final String sessionId;

    public JwtClaims(String userId, String sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
