package com.zhou6.cloud.common.context;

public class CurrentLoginUser {

    private Long userId;

    private String username;

    private String nickname;

    private String email;

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
