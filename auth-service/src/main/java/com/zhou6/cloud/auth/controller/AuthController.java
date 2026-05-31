package com.zhou6.cloud.auth.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.auth.dto.LoginRequest;
import com.zhou6.cloud.auth.dto.LogoutRequest;
import com.zhou6.cloud.auth.dto.RefreshRequest;
import com.zhou6.cloud.auth.vo.TokenResponse;
import com.zhou6.cloud.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口，负责登录、刷新令牌和退出登录。
 */
@Tag(name = "认证管理", description = "提供登录、刷新令牌和退出登录能力")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "登录", description = "校验账号密码，成功后签发访问令牌和刷新令牌")
    public R<TokenResponse> login(@RequestBody LoginRequest request,
            @RequestHeader(value = "X-Client-Ip", defaultValue = "unknown") String clientIp) {
        return R.ok(authService.login(request, clientIp), "登录成功");
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌轮换新的访问令牌和刷新令牌")
    public R<TokenResponse> refresh(@RequestBody RefreshRequest request,
            @RequestHeader(value = "X-Client-Ip", defaultValue = "unknown") String clientIp) {
        return R.ok(authService.refresh(request, clientIp), "刷新成功");
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "使指定刷新令牌和当前登录会话立即失效")
    public R<Void> logout(@RequestBody(required = false) LogoutRequest request) {
        authService.logout(request);
        return R.ok(null, "退出成功");
    }
}
