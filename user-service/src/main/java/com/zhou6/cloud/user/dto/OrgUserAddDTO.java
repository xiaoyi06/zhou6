package com.zhou6.cloud.user.dto;

import java.util.List;

import lombok.Data;

/**
 * 部门添加人员请求参数。
 */
@Data
public class OrgUserAddDTO {

    private Long orgId;

    private List<Long> userIds;

    private Short isPrimary;
}
