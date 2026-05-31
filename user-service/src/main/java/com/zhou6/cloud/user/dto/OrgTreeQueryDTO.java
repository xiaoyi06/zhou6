package com.zhou6.cloud.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 组织机构树查询请求参数。
 */
@Data
@Schema(description = "组织机构树查询请求参数")
public class OrgTreeQueryDTO {

    @Schema(description = "机构名称，支持模糊查询", example = "技")
    private String orgName;

    @Schema(description = "机构类型：1单位/公司，2部门，3班组", example = "2")
    private Short orgType;

    @Schema(description = "状态过滤：1正常，0停用；不传查询全部未删除机构", example = "1")
    private Short status;
}
