package com.zhou6.cloud.auth.service;

import com.zhou6.cloud.auth.dto.LoginRequest;
import com.zhou6.cloud.auth.dto.LogoutRequest;
import com.zhou6.cloud.auth.dto.RefreshRequest;
import com.zhou6.cloud.auth.vo.TokenResponse;

/**
 * 认证业务接口，负责令牌签发、刷新和会话失效。
 */
public interface AuthService {

    /**
     * 用户登录：校验账号密码，成功后签发短效访问令牌和长效刷新令牌。
     *
     * @param request 登录请求参数
     * @param clientIp 客户端 IP
     * @param userAgent 客户端 User-Agent
     * @return 双令牌响应
     */
    TokenResponse login(LoginRequest request, String clientIp, String userAgent);

    /**
     * 刷新令牌：校验 Redis 中的大令牌，成功后轮换新的双令牌。
     *
     * @param request 刷新令牌请求参数
     * @param clientIp 客户端 IP
     * @return 新的双令牌响应
     */
    TokenResponse refresh(RefreshRequest request, String clientIp);

    /**
     * 退出登录：使当前 refreshToken 和登录会话立即失效。
     *
     * @param request 退出请求参数
     */
    void logout(LogoutRequest request);
}
