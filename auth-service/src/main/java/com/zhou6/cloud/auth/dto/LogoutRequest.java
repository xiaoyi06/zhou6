package com.zhou6.cloud.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 退出登录请求参数。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "退出登录请求参数")
public class LogoutRequest {

    @Schema(description = "刷新令牌；传入后服务端会删除对应登录会话", example = "f4d7a0b8c1e94b13a2f4")
    private String refreshToken;
}
