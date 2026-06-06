package com.zhou6.cloud.sys;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 系统控制与安全审计服务启动类。
 */
@EnableScheduling
@MapperScan("com.zhou6.cloud.sys.mapper")
@SpringBootApplication
public class SysServiceApplication {

    /**
     * 应用启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SysServiceApplication.class, args);
    }
}
