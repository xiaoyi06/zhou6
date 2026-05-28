package com.zhou6.cloud.user.dto;

import java.util.List;

import lombok.Data;

/**
 * 角色分配菜单请求参数。
 */
@Data
public class MenuAssignDTO {

    /** 角色ID。 */
    private String roleId;

    /** 菜单ID列表。 */
    private List<String> menuIds;
}
