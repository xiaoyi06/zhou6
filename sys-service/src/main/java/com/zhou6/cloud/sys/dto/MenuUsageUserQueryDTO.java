package com.zhou6.cloud.sys.dto;

/**
 * 管理员查询指定菜单访问人员的分页参数。
 */
public class MenuUsageUserQueryDTO extends PageQueryDTO {

    /** 必填，菜单/模块标识。 */
    private String moduleName;

    /** 登录账号或昵称，支持模糊查询。 */
    private String keyword;

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
