package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 用户已分配角色分页查询参数。
 */
@Data
public class UserRoleQueryDTO {

    /** 用户ID。 */
    private String userId;

    /** 角色名称，支持模糊查询。 */
    private String roleName;

    /** 角色编码，支持模糊查询。 */
    private String roleCode;

    /** 当前页码。 */
    private Integer pageNum = 1;

    /** 每页条数。 */
    private Integer pageSize = 10;
}
