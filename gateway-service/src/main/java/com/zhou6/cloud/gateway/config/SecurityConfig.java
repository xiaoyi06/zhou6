package com.zhou6.cloud.gateway.config;

import com.zhou6.cloud.gateway.filter.JwtAuthenticationWebFilter;
import com.zhou6.cloud.gateway.support.GatewayErrorResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

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
                                errorResponseWriter.write(exchange, HttpStatus.UNAUTHORIZED, "请先登录后再访问"))
                        .accessDeniedHandler((exchange, ex) ->
                                errorResponseWriter.write(exchange, HttpStatus.FORBIDDEN, "没有权限访问该接口"))
                )
                .authorizeExchange(exchange -> exchange
                        // 登录和刷新接口必须放行，否则用户无法获取或刷新令牌。
                        .pathMatchers("/auth/login", "/auth/refresh").permitAll()
                        // 用户密码校验接口只允许 Auth 服务内部 Feign 调用，禁止外部通过网关访问。
                        .pathMatchers("/userInfo/verify").denyAll()
                        // 其他业务接口全部要求先通过 JWT 认证。
                        .anyExchange().authenticated()
                )
                // 在 Spring Security 认证阶段执行自定义 JWT 校验和用户身份透传。
                .addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
