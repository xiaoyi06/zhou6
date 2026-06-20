package com.zhou6.cloud.sys.dto;

/**
 * 登录日志分页查询参数。
 */
public class LoginLogQueryDTO extends PageQueryDTO {

    /** 登录账号，支持模糊查询。 */
    private String username;

    /** 登录状态：1-成功，0-失败，-1-账号冻结。 */
    private Integer status;

    /** 登录开始时间，格式：yyyy-MM-dd HH:mm:ss 或 ISO-8601 本地时间。 */
    private String beginTime;

    /** 登录结束时间，格式：yyyy-MM-dd HH:mm:ss 或 ISO-8601 本地时间。 */
    private String endTime;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getBeginTime() { return beginTime; }
    public void setBeginTime(String beginTime) { this.beginTime = beginTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
