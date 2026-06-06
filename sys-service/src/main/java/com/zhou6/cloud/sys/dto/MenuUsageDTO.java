package com.zhou6.cloud.sys.dto;

/**
 * 菜单使用行为投递参数。
 */
public class MenuUsageDTO {

    private String userId;
    private String menuId;
    private String menuName;
    private String routePath;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getMenuId() { return menuId; }
    public void setMenuId(String menuId) { this.menuId = menuId; }
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }
    public String getRoutePath() { return routePath; }
    public void setRoutePath(String routePath) { this.routePath = routePath; }
}
