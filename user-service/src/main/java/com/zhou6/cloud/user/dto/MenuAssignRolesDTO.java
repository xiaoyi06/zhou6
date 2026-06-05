package com.zhou6.cloud.user.dto;

import java.util.List;

import lombok.Data;

/**
 * 菜单角色分配请求参数。
 */
@Data
public class MenuAssignRolesDTO {

    /** 菜单ID。 */
    private String menuId;

    /** 角色ID列表。 */
    private List<String> roleIds;
}
