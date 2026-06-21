package com.zhou6.cloud.sys.dto;

/**
 * 删除单条登录日志的定位参数。
 */
public class LoginLogDeleteDTO {

    /** 日志主键，使用字符串避免前端大整数精度丢失。 */
    private String id;

    /** 日志写入时间，格式：yyyy-MM-dd HH:mm:ss。 */
    private String loginTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLoginTime() { return loginTime; }
    public void setLoginTime(String loginTime) { this.loginTime = loginTime; }
}
