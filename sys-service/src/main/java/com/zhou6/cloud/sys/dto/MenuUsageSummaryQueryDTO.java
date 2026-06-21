package com.zhou6.cloud.sys.dto;

/**
 * 管理员菜单访问汇总分页查询参数。
 */
public class MenuUsageSummaryQueryDTO extends PageQueryDTO {

    /** 可选，指定后只统计该用户的菜单访问情况。 */
    private String userId;

    /** 菜单/模块标识，支持模糊查询。 */
    private String moduleName;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
}
