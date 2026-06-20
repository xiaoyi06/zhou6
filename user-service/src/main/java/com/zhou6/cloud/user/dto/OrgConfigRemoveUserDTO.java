package com.zhou6.cloud.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "机构移除用户请求参数")
public class OrgConfigRemoveUserDTO {

    @Schema(description = "机构ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long orgId;

    @Schema(description = "待移除的用户ID列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[10002, 10003]")
    private List<Long> userIds;
}
