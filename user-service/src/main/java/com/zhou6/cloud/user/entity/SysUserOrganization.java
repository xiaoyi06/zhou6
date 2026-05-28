package com.zhou6.cloud.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户组织关联实体，对应 sys_user_org 表。
 */
@TableName("sys_user_org")
public class SysUserOrganization {

    /** 部门ID。 */
    private Long orgId;

    /** 用户ID。 */
    private Long userId;

    /** 是否主部门，1是，0否。 */
    private Short isPrimary;

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Short getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Short isPrimary) {
        this.isPrimary = isPrimary;
    }
}
