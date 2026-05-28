package com.zhou6.cloud.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 角色菜单关联实体，对应 sys_role_menu 表。
 */
@TableName("sys_role_menu")
public class SysRoleMenu {

    /** 角色ID。 */
    private Long roleId;

    /** 菜单ID。 */
    private Long menuId;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
}
