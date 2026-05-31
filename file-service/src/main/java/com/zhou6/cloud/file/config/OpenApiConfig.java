package com.zhou6.cloud.file.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * File 服务 OpenAPI 文档配置。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fileOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("zhou6 file-service API")
                        .description("统一文件上传、下载、删除和对象存储路由接口文档")
                        .version("1.0.0"));
    }
}
