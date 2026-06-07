package com.zhou6.cloud.sys.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 查询 sys_menu 表获取菜单路由和图标。
 */
public interface SysMenuMapper {

    /** 按菜单 ID 查询路由路径和图标。 */
    MenuRouteIcon selectRouteIconById(@Param("menuId") Long menuId);

    class MenuRouteIcon {
        private Long menuId;
        private String routePath;
        private String icon;
        private String menuName;

        public Long getMenuId() { return menuId; }
        public void setMenuId(Long menuId) { this.menuId = menuId; }
        public String getRoutePath() { return routePath; }
        public void setRoutePath(String routePath) { this.routePath = routePath; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public String getMenuName() { return menuName; }
        public void setMenuName(String menuName) { this.menuName = menuName; }
    }
}
