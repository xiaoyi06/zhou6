package com.zhou6.cloud.auth.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 客户端配置：自动透传网关下发的客户端 IP，供下游微服务记录登录来源。
 */
@Configuration
public class FeignConfig {

    /**
     * 将当前请求的 X-Client-Ip 请求头透传到 Feign 调用中，
     * 确保 user-service 能获取到客户端真实 IP 而非 auth-service 的本地地址。
     */
    @Bean
    public RequestInterceptor clientIpInterceptor() {
        return (RequestTemplate template) -> {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes sra) {
                String clientIp = sra.getRequest().getHeader("X-Client-Ip");
                if (clientIp != null && !clientIp.isBlank()) {
                    template.header("X-Client-Ip", clientIp);
                }
            }
        };
    }
}
