package com.zhou6.cloud.user.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * 部门树节点响应对象。
 */
@Data
public class OrgTreeVO {

    private Long id;

    private Long parentId;

    private String orgName;

    private Short orgType;

    private Short status;

    private Integer sortOrder;

    private List<OrgTreeVO> children = new ArrayList<>();
}
