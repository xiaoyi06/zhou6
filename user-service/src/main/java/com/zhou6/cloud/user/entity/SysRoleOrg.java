package com.zhou6.cloud.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 角色机构数据权限关联实体，对应 sys_role_org 表。
 */
@TableName("sys_role_org")
public class SysRoleOrg {

    /** 角色ID。 */
    private Long roleId;

    /** 机构或部门ID。 */
    private Long orgId;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }
}
