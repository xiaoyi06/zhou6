package com.zhou6.cloud.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新令牌请求参数。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "刷新令牌请求参数")
public class RefreshRequest {

    @Schema(description = "刷新令牌", requiredMode = Schema.RequiredMode.REQUIRED, example = "f4d7a0b8c1e94b13a2f4")
    private String refreshToken;
}
