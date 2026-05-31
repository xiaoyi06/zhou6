package com.zhou6.cloud.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户服务账号密码校验请求参数。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户服务账号密码校验请求参数")
public class VerifyRequest {

    @Schema(description = "登录账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    private String username;

    @Schema(description = "登录密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String password;
}
