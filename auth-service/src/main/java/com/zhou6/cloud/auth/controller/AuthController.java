package com.zhou6.cloud.auth.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.auth.dto.LoginRequest;
import com.zhou6.cloud.auth.dto.LogoutRequest;
import com.zhou6.cloud.auth.dto.RefreshRequest;
import com.zhou6.cloud.auth.dto.TokenResponse;
import com.zhou6.cloud.auth.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public R<TokenResponse> login(@RequestBody LoginRequest request,
            @RequestHeader(value = "X-Client-Ip", defaultValue = "unknown") String clientIp) {
        return R.ok(authService.login(request, clientIp), "登录成功");
    }

    @PostMapping("/refresh")
    public R<TokenResponse> refresh(@RequestBody RefreshRequest request,
            @RequestHeader(value = "X-Client-Ip", defaultValue = "unknown") String clientIp) {
        return R.ok(authService.refresh(request, clientIp), "刷新成功");
    }

    @PostMapping("/logout")
    public R<Void> logout(@RequestBody(required = false) LogoutRequest request) {
        authService.logout(request);
        return R.ok(null, "退出成功");
    }
}
