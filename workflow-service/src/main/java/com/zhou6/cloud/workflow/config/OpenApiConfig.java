package com.zhou6.cloud.workflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * workflow-service OpenAPI 文档配置。
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置工作流服务 Swagger 文档基础信息。
     *
     * @return OpenAPI 文档对象
     */
    @Bean
    public OpenAPI workflowOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("zhou6 workflow-service API")
                        .description("工作流服务接口文档")
                        .version("1.0.0"));
    }
}
