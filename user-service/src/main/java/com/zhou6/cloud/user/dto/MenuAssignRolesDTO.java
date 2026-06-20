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

    /** 完整的已分配角色ID列表；传入空列表时清空该菜单的全部角色配置。 */
    private List<String> roleIds;
}
