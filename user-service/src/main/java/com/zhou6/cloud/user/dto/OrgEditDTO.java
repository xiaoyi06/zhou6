package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 修改部门请求参数。
 */
@Data
public class OrgEditDTO {

    private Long id;

    private Long parentId;

    private String orgName;

    private Short orgType;

    private Short status;

    private Integer sortOrder;
}
