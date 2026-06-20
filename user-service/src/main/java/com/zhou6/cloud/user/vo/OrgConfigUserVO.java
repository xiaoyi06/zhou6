package com.zhou6.cloud.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "机构用户响应对象")
public class OrgConfigUserVO {

    @Schema(description = "用户ID", example = "10001")
    private String userId;

    @Schema(description = "登录账号", example = "zhangsan")
    private String username;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "联系电话", example = "13800000000")
    private String contactPhone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "性别，0未知，1男，2女", example = "1")
    private Integer gender;

    @Schema(description = "主部门ID", example = "10001")
    private String primaryOrgId;

    @Schema(description = "主部门名称", example = "技术部")
    private String primaryOrgName;

    @Schema(description = "头像文件ID", example = "f_abc123")
    private String avatarFileId;

    @Schema(description = "个性签名", example = "天道酬勤")
    private String personalSignature;

    @Schema(description = "工作状态", example = "在职")
    private String workStatus;

    @Schema(description = "账号状态，1正常，0禁用", example = "1")
    private Integer status;

    @Schema(description = "最后登录IP", example = "192.168.1.1")
    private String lastLoginIp;

    @Schema(description = "最后登录时间", example = "2026-06-20 10:30:00")
    private String lastLoginTime;

    @Schema(description = "创建时间", example = "2026-01-01 00:00:00")
    private String createTime;
}
