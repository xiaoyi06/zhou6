package com.zhou6.cloud.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设置用户主组织机构请求参数。
 */
@Data
@Schema(description = "设置用户主组织机构请求参数")
public class OrgUserSetPrimaryDTO {

    @Schema(description = "机构ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long orgId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10002")
    private Long userId;
}
