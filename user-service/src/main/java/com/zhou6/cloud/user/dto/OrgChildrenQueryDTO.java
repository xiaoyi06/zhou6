package com.zhou6.cloud.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 子级组织机构查询请求参数。
 */
@Data
@Schema(description = "子级组织机构查询请求参数")
public class OrgChildrenQueryDTO {

    @Schema(description = "父级机构ID，查询顶级机构传0或不传", example = "0")
    private Long parentId;

    @Schema(description = "机构名称，支持模糊查询", example = "技")
    private String orgName;

    @Schema(description = "机构类型：1单位/公司，2部门，3班组", example = "2")
    private Short orgType;

    @Schema(description = "状态过滤：1正常，0停用；不传查询全部未删除机构", example = "1")
    private Short status;
}
