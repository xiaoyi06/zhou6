package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 角色分页查询请求参数。
 */
@Data
public class RoleQueryDTO {

    /** 角色名称，支持模糊查询。 */
    private String roleName;

    /** 角色编码，支持模糊查询。 */
    private String roleCode;

    /** 所属外部系统ID。 */
    private String systemId;

    /** 角色状态，1正常，0停用。 */
    private Integer status;

    /** 当前页码。 */
    private Integer pageNum = 1;

    /** 每页条数。 */
    private Integer pageSize = 10;
}
