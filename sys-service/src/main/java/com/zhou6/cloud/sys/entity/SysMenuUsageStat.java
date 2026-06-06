package com.zhou6.cloud.sys.entity;

import java.time.LocalDateTime;

/**
 * 当前用户常用菜单统计实体，对应 sys_menu_usage_stat 表。
 */
public class SysMenuUsageStat {

    private Long userId;
    private Long menuId;
    private String menuName;
    private String routePath;
    private Long useCount;
    private LocalDateTime lastUseTime;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }
    public String getRoutePath() { return routePath; }
    public void setRoutePath(String routePath) { this.routePath = routePath; }
    public Long getUseCount() { return useCount; }
    public void setUseCount(Long useCount) { this.useCount = useCount; }
    public LocalDateTime getLastUseTime() { return lastUseTime; }
    public void setLastUseTime(LocalDateTime lastUseTime) { this.lastUseTime = lastUseTime; }
}
