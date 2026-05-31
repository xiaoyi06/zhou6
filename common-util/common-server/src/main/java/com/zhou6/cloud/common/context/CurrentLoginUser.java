package com.zhou6.cloud.common.context;

/**
 * 当前登录用户上下文对象。
 */
public class CurrentLoginUser {

    /** 当前登录用户 ID。 */
    private Long userId;

    /** 登录账号。 */
    private String username;

    /** 用户昵称。 */
    private String nickname;

    /** 邮箱。 */
    private String email;

    /** 联系电话。 */
    private String contactPhone;

    public CurrentLoginUser() {
    }

    public CurrentLoginUser(Long userId, String username, String nickname, String email, String contactPhone) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.contactPhone = contactPhone;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
