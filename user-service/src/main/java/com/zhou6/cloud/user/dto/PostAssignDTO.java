package com.zhou6.cloud.user.dto;

import java.util.List;

import lombok.Data;

/**
 * 岗位批量分配用户请求参数。
 */
@Data
public class PostAssignDTO {

    /** 岗位ID。 */
    private String postId;

    /** 部门ID，用于限定岗位生效部门。 */
    private String orgId;

    /** 用户ID列表。 */
    private List<String> userIds;
}
