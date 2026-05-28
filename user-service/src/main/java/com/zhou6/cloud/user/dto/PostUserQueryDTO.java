package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 岗位下用户查询请求参数。
 */
@Data
public class PostUserQueryDTO {

    /** 岗位ID。 */
    private String postId;

    /** 部门ID，为空时查询该岗位全部用户。 */
    private String orgId;
}
