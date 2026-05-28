package com.zhou6.cloud.user.dto;

import lombok.Data;

/**
 * 部门人员分页查询请求参数。
 */
@Data
public class OrgUserPageDTO {

    private Long orgId;

    private Integer pageNum;

    private Integer pageSize;
}
