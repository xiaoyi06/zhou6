package com.zhou6.cloud.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 组织机构 ID 请求参数。
 */
@Data
@Schema(description = "组织机构ID请求参数")
public class OrgIdDTO {

    @Schema(description = "机构ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long id;
}
