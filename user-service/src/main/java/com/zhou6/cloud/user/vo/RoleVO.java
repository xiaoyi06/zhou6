package com.zhou6.cloud.user.vo;

import java.util.List;

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

    /** 所属外部系统ID。 */
    private String systemId;

    /** 所属外部系统名称。 */
    private String systemName;

    /** 数据权限范围。 */
    private Integer dataScope;

    /** 已配置的自定义数据权限机构ID列表。 */
    private List<String> orgIds;

    /** 排序号。 */
    private Integer sortOrder;

    /** 角色状态，1正常，0停用。 */
    private Integer status;

    /** 备注。 */
    private String remark;
}
