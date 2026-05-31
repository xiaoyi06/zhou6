package com.zhou6.cloud.file.storage.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.file.config.StorageProperties;
import com.zhou6.cloud.file.storage.FileStorageService;

/**
 * 文件存储公共基类，封装平台标识、对象键生成、访问地址拼接和基础配置校验。
 */
public abstract class AbstractFileStorageService implements FileStorageService {

    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final String platform;
    protected final StorageProperties.Provider provider;

    protected AbstractFileStorageService(String platform, StorageProperties.Provider provider) {
        this.platform = platform;
        this.provider = provider;
        requireText(provider.getEndpoint(), "对象存储 endpoint 不能为空");
        requireText(provider.getAccessKey(), "对象存储 access-key 不能为空");
        requireText(provider.getSecretKey(), "对象存储 secret-key 不能为空");
        requireText(provider.getBucketName(), "对象存储 bucket-name 不能为空");
    }

    @Override
    public String getPlatform() {
        return platform;
    }

    /**
     * 生成对象键：日期目录 + UUID + 原始扩展名，降低单目录对象数量并避免文件名冲突。
     *
     * @param fileName 原始文件名
     * @return 对象存储中的唯一键
     */
    protected String buildObjectKey(String fileName) {
        String suffix = "";
        if (fileName != null) {
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
                suffix = fileName.substring(dotIndex);
            }
        }
        return DATE_PATH.format(LocalDate.now()) + "/" + UUID.randomUUID().toString().replace("-", "") + suffix;
    }

    /**
     * 构建访问地址。path-style 会在地址中拼接 bucket，virtual-host style 直接拼接 objectKey。
     *
     * @param objectKey 对象存储中的唯一键
     * @return 文件访问地址
     */
    protected String buildUrl(String objectKey) {
        String baseUrl = provider.getPublicEndpoint();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = provider.getEndpoint();
        }
        baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        if (provider.isPathStyleAccess()) {
            return baseUrl + "/" + provider.getBucketName() + "/" + objectKey;
        }
        return baseUrl + "/" + objectKey;
    }

    /**
     * 非空字符串断言。
     */
    protected void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, message);
        }
    }
}
