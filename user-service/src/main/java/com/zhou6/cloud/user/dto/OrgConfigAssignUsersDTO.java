package com.zhou6.cloud.user.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "机构批量分配用户请求参数")
public class OrgConfigAssignUsersDTO {

    @Schema(description = "机构ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long orgId;

    @Schema(description = "用户ID列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[10001, 10002]")
    private List<Long> userIds;
}
