package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 新增部门请求参数。
 */
@Data
public class OrgAddDTO {

    private Long parentId;

    private String orgName;

    private Short orgType;

    private Short status;

    private Integer sortOrder;
}
