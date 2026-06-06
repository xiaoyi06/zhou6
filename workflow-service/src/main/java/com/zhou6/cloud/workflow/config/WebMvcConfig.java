package com.zhou6.cloud.workflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 工作流服务 Web MVC 配置。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserContextInterceptor userContextInterceptor;

    public WebMvcConfig(UserContextInterceptor userContextInterceptor) {
        this.userContextInterceptor = userContextInterceptor;
    }

    /**
     * 注册用户上下文拦截器，让工作流接口可以从 JWT 中恢复当前操作人。
     *
     * @param registry Spring MVC 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor).addPathPatterns("/**");
    }
}
