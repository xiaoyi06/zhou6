package com.zhou6.cloud.account.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * account-service OpenAPI 文档配置。
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置账户服务 Swagger 文档基础信息。
     *
     * @return OpenAPI 文档对象
     */
    @Bean
    public OpenAPI accountOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("zhou6 account-service API")
                        .description("现金账户服务接口文档")
                        .version("1.0.0"));
    }
}
