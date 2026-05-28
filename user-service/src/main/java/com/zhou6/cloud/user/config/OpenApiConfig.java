package com.zhou6.cloud.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * User 服务 OpenAPI 文档配置。
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置 User 服务 Swagger 文档基础信息。
     *
     * @return OpenAPI 文档对象
     */
    @Bean
    public OpenAPI userOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("zhou6 user-service API")
                        .description("用户、组织、岗位、角色、菜单等接口文档")
                        .version("1.0.0"));
    }
}
