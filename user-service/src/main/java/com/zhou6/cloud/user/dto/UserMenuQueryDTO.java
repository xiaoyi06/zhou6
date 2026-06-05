package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 用户菜单权限查询请求参数。
 */
@Data
public class UserMenuQueryDTO {

    /** 用户ID，兼容旧入参 id 字段。 */
    private String id;

    /** 用户ID。 */
    private String userId;

    /** 菜单状态，1正常，0停用。 */
    private Integer status;
}
