package com.zhou6.cloud.file;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 文件服务启动类，负责启动文件上传微服务并扫描本模块 Mapper。
 */
@MapperScan("com.zhou6.cloud.file.mapper")
@SpringBootApplication
public class FileServiceApplication {

    /**
     * 应用启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class, args);
    }
}
