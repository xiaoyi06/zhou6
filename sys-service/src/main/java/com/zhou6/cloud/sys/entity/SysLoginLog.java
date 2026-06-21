package com.zhou6.cloud.sys.entity;

import java.time.LocalDateTime;

/**
 * 系统登录记录实体，对应 sys_login_log 分区表。
 */
public class SysLoginLog {

    private String id;
    private Long userId;
    private String username;
    private String ipAddress;
    private String loginLocation;
    private String browser;
    private String os;
    private Short status;
    private String msg;
    private LocalDateTime loginTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
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
