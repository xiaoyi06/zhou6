package com.zhou6.cloud.user.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 组织机构添加人员请求参数。
 */
@Data
@Schema(description = "组织机构添加人员请求参数")
public class OrgUserAddDTO {

    @Schema(description = "机构ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long orgId;

    @Schema(description = "待添加的用户ID列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[10001,10002]")
    private List<Long> userIds;

    @Schema(description = "是否设置为主部门：1是，0否；不传默认0", example = "0")
    private Short isPrimary;
}
