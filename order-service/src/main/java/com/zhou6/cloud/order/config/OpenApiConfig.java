package com.zhou6.cloud.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * order-service OpenAPI 文档配置。
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置订单服务 Swagger 文档基础信息。
     *
     * @return OpenAPI 文档对象
     */
    @Bean
    public OpenAPI orderOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("zhou6 order-service API")
                        .description("订单状态机、支付、取消和退款接口文档")
                        .version("1.0.0"));
    }
}
