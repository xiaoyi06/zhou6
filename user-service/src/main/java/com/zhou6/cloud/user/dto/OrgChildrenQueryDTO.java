package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 子部门查询请求参数。
 */
@Data
public class OrgChildrenQueryDTO {

    private Long parentId;

    private Short status;
}
