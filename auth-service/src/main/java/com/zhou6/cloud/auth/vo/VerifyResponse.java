package com.zhou6.cloud.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户服务账号密码校验响应对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户服务账号密码校验响应对象")
public class VerifyResponse {

    @Schema(description = "是否校验通过", example = "true")
    private boolean verified;

    @Schema(description = "用户ID，字符串格式避免前端大整数精度丢失", example = "10001")
    private String userId;

    @Schema(description = "登录账号", example = "admin")
    private String username;

    @Schema(description = "用户昵称", example = "管理员")
    private String nickname;

    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "联系电话", example = "13800000000")
    private String contactPhone;

    @Schema(description = "校验失败原因；成功时为空", example = "账号或密码错误")
    private String message;
}
