package com.zhou6.cloud.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 组织树用户节点响应对象。
 */
@Data
@Schema(description = "组织树用户节点响应对象")
public class UserTreeNodeVO {

    @Schema(description = "用户ID，字符串格式返回避免前端大整数精度丢失", example = "10001")
    private String id;

    @Schema(description = "登录账号", example = "zhangsan")
    private String username;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "性别，0未知，1男，2女", example = "1")
    private Integer gender;

    @Schema(description = "账号状态，1正常，0禁用", example = "1")
    private Integer status;
}
