package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 设置用户主部门请求参数。
 */
@Data
public class OrgUserSetPrimaryDTO {

    private Long orgId;

    private Long userId;
}
