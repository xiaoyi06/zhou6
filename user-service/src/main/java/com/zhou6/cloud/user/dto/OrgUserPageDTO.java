package com.zhou6.cloud.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 组织机构人员分页查询请求参数。
 */
@Data
@Schema(description = "组织机构人员分页查询请求参数")
public class OrgUserPageDTO {

    @Schema(description = "机构ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long orgId;

    @Schema(description = "页码，从1开始；不传默认1", example = "1")
    private Integer pageNum;

    @Schema(description = "每页数量；不传默认10", example = "10")
    private Integer pageSize;
}
