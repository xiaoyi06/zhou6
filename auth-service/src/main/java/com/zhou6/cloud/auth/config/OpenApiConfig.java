package com.zhou6.cloud.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auth 服务 OpenAPI 文档配置。
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置 Auth 服务 Swagger 文档基础信息。
     *
     * @return OpenAPI 文档对象
     */
    @Bean
    public OpenAPI authOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("zhou6 auth-service API")
                        .description("认证服务接口文档")
                        .version("1.0.0"));
    }
}
