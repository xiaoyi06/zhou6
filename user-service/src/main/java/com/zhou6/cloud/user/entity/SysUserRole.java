package com.zhou6.cloud.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户角色关联实体，对应 sys_user_role 表。
 */
@TableName("sys_user_role")
public class SysUserRole {

    /** 用户ID。 */
    private Long userId;

    /** 角色ID。 */
    private Long roleId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
