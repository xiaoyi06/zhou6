package com.zhou6.cloud.user.vo;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 组织机构用户树节点响应对象，包含子级组织和归属用户。
 */
@Data
@Schema(description = "组织机构用户树节点响应对象")
public class OrgUserTreeVO {

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

    @Schema(description = "显示顺序，数值越小越靠前", example = "10")
    private Integer sortOrder;

    @Schema(description = "创建时间", example = "2026-05-31 18:30:00")
    private String createTime;

    @Schema(description = "修改时间", example = "2026-05-31 18:35:00")
    private String updateTime;

    @Schema(description = "子级组织机构列表")
    private List<OrgUserTreeVO> children = new ArrayList<>();

    @Schema(description = "归属本机构的用户列表（仅主部门）")
    private List<UserTreeNodeVO> users = new ArrayList<>();
}
