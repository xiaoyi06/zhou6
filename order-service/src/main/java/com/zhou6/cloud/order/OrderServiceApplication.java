package com.zhou6.cloud.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 订单服务启动类，负责启动订单状态机微服务并扫描本模块 Mapper。
 */
@EnableScheduling
@EnableFeignClients
@MapperScan("com.zhou6.cloud.order.mapper")
@SpringBootApplication
public class OrderServiceApplication {

    /**
     * 应用启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
