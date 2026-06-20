package com.zhou6.cloud.user.dto;

import lombok.Data;

import java.util.List;

/**
 * 取消用户岗位请求参数。
 */
@Data
public class PostRemoveUserDTO {

    /** 岗位ID。 */
    private String postId;

    /** 待取消岗位的用户ID列表。 */
    private List<String> userIds;

    /** 部门ID，用于精确删除矩阵岗位关系。 */
    private String orgId;
}
