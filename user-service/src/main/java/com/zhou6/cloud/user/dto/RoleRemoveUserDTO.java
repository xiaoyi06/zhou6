package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 取消用户角色请求参数。
 */
@Data
public class RoleRemoveUserDTO {

    /** 角色ID。 */
    private String roleId;

    /** 用户ID。 */
    private String userId;
}
