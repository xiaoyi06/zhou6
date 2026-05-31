package com.zhou6.cloud.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录或刷新令牌响应对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录或刷新令牌响应对象")
public class TokenResponse {

    @Schema(description = "访问令牌，用于访问业务接口", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "刷新令牌，用于换取新的访问令牌", example = "f4d7a0b8c1e94b13a2f4")
    private String refreshToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "访问令牌有效期，单位秒", example = "50000")
    private long expiresIn;
}
