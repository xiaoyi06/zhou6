package com.zhou6.cloud.sys.vo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 登录日志查询响应，所有 ID 字段使用字符串避免前端大整数精度丢失。
 */
public class LoginLogVO {

    private String id;
    private String userId;
    private String username;
    private String ipAddress;
    private String loginLocation;
    private String browser;
    private String os;
    private Short status;
    private String msg;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getLoginLocation() { return loginLocation; }
    public void setLoginLocation(String loginLocation) { this.loginLocation = loginLocation; }
    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }
    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }
    public Short getStatus() { return status; }
    public void setStatus(Short status) { this.status = status; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
}
