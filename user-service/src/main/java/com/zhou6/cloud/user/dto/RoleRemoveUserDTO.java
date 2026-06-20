package com.zhou6.cloud.user.dto;

import lombok.Data;

import java.util.List;

/**
 * 取消用户角色请求参数。
 */
@Data
public class RoleRemoveUserDTO {

    /** 角色ID。 */
    private String roleId;

    /** 待取消角色的用户ID列表。 */
    private List<String> userIds;
}
