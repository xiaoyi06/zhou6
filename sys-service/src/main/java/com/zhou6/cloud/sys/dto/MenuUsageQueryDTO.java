package com.zhou6.cloud.sys.dto;

/**
 * 常用菜单查询参数。
 */
public class MenuUsageQueryDTO {

    private String userId;
    private Integer limit;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
}
