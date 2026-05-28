package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 角色响应对象。
 */
@Data
public class RoleVO {

    /** 角色ID。 */
    private String id;

    /** 角色名称。 */
    private String roleName;

    /** 角色编码。 */
    private String roleCode;

    /** 数据权限范围。 */
    private Integer dataScope;

    /** 排序号。 */
    private Integer sortOrder;

    /** 角色状态，1正常，0停用。 */
    private Integer status;

    /** 备注。 */
    private String remark;
}
