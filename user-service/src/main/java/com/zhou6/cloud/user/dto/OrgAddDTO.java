package com.zhou6.cloud.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 新增组织机构请求参数。
 */
@Data
@Schema(description = "新增组织机构请求参数")
public class OrgAddDTO {

    @Schema(description = "父级机构ID，顶级机构传0或不传", example = "0")
    private Long parentId;

    @Schema(description = "机构名称，如某某总公司、研发部、测试一组", requiredMode = Schema.RequiredMode.REQUIRED, example = "研发部")
    private String orgName;

    @Schema(description = "机构类型：1单位/公司，2部门，3班组", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Short orgType;

    @Schema(description = "机构编码，用于业务核算或唯一标识，非空时全表唯一", example = "RD001")
    private String orgCode;

    @Schema(description = "负责人用户ID，用于审批流或任务路由", example = "10001")
    private Long leaderId;

    @Schema(description = "状态：1正常，0停用；不传默认1", example = "1")
    private Short status;

    @Schema(description = "显示顺序，数值越小越靠前；不传默认0", example = "10")
    private Integer sortOrder;
}
