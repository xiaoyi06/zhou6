package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 部门详情响应对象。
 */
@Data
public class OrgDetailVO {

    private Long id;

    private Long parentId;

    private String orgName;

    private Short orgType;

    private Short status;

    private String treePath;

    private Integer treeLevel;

    private Integer sortOrder;
}
