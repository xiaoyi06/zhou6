package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 部门状态修改请求参数。
 */
@Data
public class OrgChangeStatusDTO {

    private Long id;

    private Short status;
}
