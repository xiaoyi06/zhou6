package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 部门移除人员请求参数。
 */
@Data
public class OrgUserRemoveDTO {

    private Long orgId;

    private Long userId;
}
