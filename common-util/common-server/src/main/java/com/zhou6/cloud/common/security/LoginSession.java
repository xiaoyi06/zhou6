package com.zhou6.cloud.common.security;

/**
 * 登录会话快照，存储在 Redis 中供网关和业务服务读取。
 */
public class LoginSession {

    /** 当前登录用户 ID。 */
    private String userId;

    /** 当前登录会话 ID。 */
    private String sessionId;

    /** 登录账号。 */
    private String username;

    /** 用户昵称。 */
    private String nickname;

    /** 邮箱。 */
    private String email;

    /** 联系电话。 */
    private String contactPhone;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
}
