package com.zhou6.cloud.account;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 账户服务启动类，负责启动现金账户微服务并扫描本模块 Mapper。
 */
@MapperScan("com.zhou6.cloud.account.mapper")
@SpringBootApplication
public class AccountServiceApplication {

    /**
     * 应用启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
