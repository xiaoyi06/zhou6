package com.zhou6.cloud.gateway.config;

import com.zhou6.cloud.common.constant.ApiPathConstants;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.gateway.filter.JwtAuthenticationWebFilter;
import com.zhou6.cloud.gateway.support.GatewayErrorResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String AUTH_API = ApiPathConstants.API_V1 + "/auth-api";
    private static final String USER_INFO_API = ApiPathConstants.API_V1 + "/user-api/userInfo";

    private final JwtAuthenticationWebFilter jwtAuthenticationWebFilter;
    private final GatewayErrorResponseWriter errorResponseWriter;

    public SecurityConfig(JwtAuthenticationWebFilter jwtAuthenticationWebFilter,
            GatewayErrorResponseWriter errorResponseWriter) {
        this.jwtAuthenticationWebFilter = jwtAuthenticationWebFilter;
        this.errorResponseWriter = errorResponseWriter;
    }

    /**
     * 配置网关响应式安全链：统一管理白名单、禁止路径和 JWT 认证过滤器。
     *
     * @return 网关安全过滤链
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain() {
        return ServerHttpSecurity.http()
                // 网关只做 API 转发，不使用浏览器表单和 CSRF 会话保护。
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((exchange, ex) ->
                                errorResponseWriter.write(exchange, CommonErrorCode.TOKEN_INVALID))
                        .accessDeniedHandler((exchange, ex) ->
                                errorResponseWriter.write(exchange, CommonErrorCode.FORBIDDEN))
                )
                .authorizeExchange(exchange -> exchange
                        // Swagger / OpenAPI 文档资源放行，便于开发联调查看接口文档。
                        .pathMatchers("/v3/api-docs/**", "/auth/v3/api-docs", "/user/v3/api-docs", "/file/v3/api-docs",
                                "/account/v3/api-docs", "/order/v3/api-docs",
                                "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
                        // 登录、刷新和退出接口必须放行，否则用户无法获取、刷新或主动失效令牌。
                        .pathMatchers(AUTH_API + "/login",
                                AUTH_API + "/refresh",
                                AUTH_API + "/logout").permitAll()
                        // 用户密码校验接口只允许 Auth 服务内部 Feign 调用，禁止外部通过网关访问。
                        .pathMatchers(USER_INFO_API + "/verify").denyAll()
                        // 待办内部接口仅供服务内网调用，不能由用户携带 JWT 经网关转发访问。
                        .pathMatchers("/api/v1/sys-api/internal/**").denyAll()
                        // 其他业务接口全部要求先通过 JWT 认证。
                        .anyExchange().authenticated()
                )
                // 在 Spring Security 认证阶段执行自定义 JWT 校验和用户身份透传。
                .addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
