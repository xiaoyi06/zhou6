package com.zhou6.cloud.user.dto;

import java.util.List;

import lombok.Data;

/**
 * 角色批量分配用户请求参数。
 */
@Data
public class RoleAssignUsersDTO {

    /** 角色ID。 */
    private String roleId;

    /** 用户ID列表。 */
    private List<String> userIds;
}
