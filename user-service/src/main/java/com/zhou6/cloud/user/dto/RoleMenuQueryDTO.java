package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 角色已配置菜单查询请求参数。
 */
@Data
public class RoleMenuQueryDTO {

    /** 角色ID。 */
    private String roleId;

    /** 菜单状态，1正常，0停用。 */
    private Integer status;
}
