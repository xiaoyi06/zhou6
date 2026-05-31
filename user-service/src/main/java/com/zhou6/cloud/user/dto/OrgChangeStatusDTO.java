package com.zhou6.cloud.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 组织机构状态修改请求参数。
 */
@Data
@Schema(description = "组织机构状态修改请求参数")
public class OrgChangeStatusDTO {

    @Schema(description = "机构ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long id;

    @Schema(description = "状态：1正常，0停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Short status;
}
