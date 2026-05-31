package com.zhou6.cloud.file.config;

import com.zhou6.cloud.file.storage.FileStorageService;
import com.zhou6.cloud.file.storage.impl.CosStorageServiceImpl;
import com.zhou6.cloud.file.storage.impl.MinioStorageServiceImpl;
import com.zhou6.cloud.file.storage.impl.ObsStorageServiceImpl;
import com.zhou6.cloud.file.storage.impl.OssStorageServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 按 storage.type 条件装配唯一的文件存储策略。
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfiguration {

    /**
     * MinIO 存储策略，未显式配置 storage.type 时作为默认实现。
     *
     * @param properties 存储配置
     * @return MinIO 文件存储服务
     */
    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "minio", matchIfMissing = true)
    public FileStorageService minioStorageService(StorageProperties properties) {
        return new MinioStorageServiceImpl(properties.getMinio());
    }

    /**
     * 华为云 OBS 存储策略。
     *
     * @param properties 存储配置
     * @return OBS 文件存储服务
     */
    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "obs")
    public FileStorageService obsStorageService(StorageProperties properties) {
        return new ObsStorageServiceImpl(properties.getObs());
    }

    /**
     * 阿里云 OSS 存储策略。
     *
     * @param properties 存储配置
     * @return OSS 文件存储服务
     */
    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "oss")
    public FileStorageService ossStorageService(StorageProperties properties) {
        return new OssStorageServiceImpl(properties.getOss());
    }

    /**
     * 腾讯云 COS 存储策略。
     *
     * @param properties 存储配置
     * @return COS 文件存储服务
     */
    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "cos")
    public FileStorageService cosStorageService(StorageProperties properties) {
        return new CosStorageServiceImpl(properties.getCos());
    }

}
