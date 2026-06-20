package com.zhou6.cloud.user.dto;

import java.util.List;

import lombok.Data;

/**
 * 用户角色全量分配参数。
 */
@Data
public class UserAssignRolesDTO {

    /** 用户ID。 */
    private String userId;

    /** 用户最终拥有的全部角色ID；传空列表时清空全部角色。 */
    private List<String> roleIds;
}
