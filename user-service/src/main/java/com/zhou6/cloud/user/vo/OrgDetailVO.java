package com.zhou6.cloud.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 组织机构详情响应对象。
 */
@Data
@Schema(description = "组织机构详情响应对象")
public class OrgDetailVO {

    @Schema(description = "机构ID，字符串格式返回避免前端大整数精度丢失", example = "10001")
    private String id;

    @Schema(description = "父级机构ID，顶级机构为0", example = "0")
    private String parentId;

    @Schema(description = "机构名称", example = "研发部")
    private String orgName;

    @Schema(description = "机构类型：1单位/公司，2部门，3班组", example = "2")
    private Short orgType;

    @Schema(description = "机构编码", example = "RD001")
    private String orgCode;

    @Schema(description = "负责人用户ID，字符串格式返回避免前端大整数精度丢失", example = "10001")
    private String leaderId;

    @Schema(description = "负责人用户名称", example = "张三")
    private String leaderName;

    @Schema(description = "状态：1正常，0停用", example = "1")
    private Short status;

    @Schema(description = "树路径，格式如0,100,101", example = "0,10001,10002")
    private String treePath;

    @Schema(description = "树层级，顶级机构为1", example = "2")
    private Integer treeLevel;

    @Schema(description = "显示顺序，数值越小越靠前", example = "10")
    private Integer sortOrder;

    @Schema(description = "逻辑删除：0未删除，1已删除", example = "0")
    private Short isDeleted;

    @Schema(description = "创建时间", example = "2026-05-31 18:30:00")
    private String createTime;

    @Schema(description = "修改时间", example = "2026-05-31 18:35:00")
    private String updateTime;
}
