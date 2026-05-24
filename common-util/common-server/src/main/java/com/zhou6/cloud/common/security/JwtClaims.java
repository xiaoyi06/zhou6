package com.zhou6.cloud.common.security;

public class JwtClaims {

    private final String userId;

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
