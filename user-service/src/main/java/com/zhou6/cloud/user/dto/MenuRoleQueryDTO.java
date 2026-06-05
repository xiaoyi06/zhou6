package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 菜单已配置角色查询请求参数。
 */
@Data
public class MenuRoleQueryDTO {

    /** 菜单ID，兼容旧入参 id 字段。 */
    private String id;

    /** 菜单ID。 */
    private String menuId;

    /** 角色名称。 */
    private String roleName;

    /** 角色编码。 */
    private String roleCode;

    /** 角色状态，1正常，0停用。 */
    private Integer status;

    /** 当前页码。 */
    private Integer pageNum;

    /** 每页数量。 */
    private Integer pageSize;
}
