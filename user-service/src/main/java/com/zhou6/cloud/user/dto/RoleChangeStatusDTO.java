package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 角色启停请求参数。
 */
@Data
public class RoleChangeStatusDTO {

    /** 角色ID。 */
    private String id;

    /** 角色状态，1正常，0停用。 */
    private Integer status;
}
