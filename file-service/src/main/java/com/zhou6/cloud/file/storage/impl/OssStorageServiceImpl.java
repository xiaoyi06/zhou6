package com.zhou6.cloud.file.storage.impl;

import java.io.InputStream;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.file.config.StorageProperties;
import com.zhou6.cloud.file.storage.StoredFile;

/**
 * 阿里云 OSS 文件存储实现，使用 OSS 官方 SDK 上传、下载和删除对象。
 */
public class OssStorageServiceImpl extends AbstractFileStorageService {

    private final OSS ossClient;

    /**
     * 创建 OSS 存储服务。
     *
     * @param provider OSS 连接配置
     */
    public OssStorageServiceImpl(StorageProperties.Provider provider) {
        super("oss", provider);
        this.ossClient = new OSSClientBuilder()
                .build(provider.getEndpoint(), provider.getAccessKey(), provider.getSecretKey());
    }

    /**
     * 使用 OSS SDK 的 putObject 上传文件流。
     */
    @Override
    public StoredFile upload(InputStream inputStream, String fileName, String contentType, long fileSize) {
        String objectKey = buildObjectKey(fileName);
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileSize);
            metadata.setContentType(contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType);
            ossClient.putObject(provider.getBucketName(), objectKey, inputStream, metadata);
            return new StoredFile(objectKey, buildUrl(objectKey));
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "OSS 文件上传失败", ex);
        }
    }

    /**
     * 使用 OSS SDK 的 getObject 下载文件流。
     */
    @Override
    public InputStream download(String objectKey) {
        try {
            OSSObject object = ossClient.getObject(provider.getBucketName(), objectKey);
            return object.getObjectContent();
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "OSS 文件下载失败", ex);
        }
    }

    /**
     * 使用 OSS SDK 的 deleteObject 删除文件。
     */
    @Override
    public void delete(String objectKey) {
        try {
            ossClient.deleteObject(provider.getBucketName(), objectKey);
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "OSS 文件删除失败", ex);
        }
    }

    /**
     * 使用 OSS SDK 的 doesObjectExist 判断对象是否存在。
     */
    @Override
    public boolean exists(String objectKey) {
        try {
            return ossClient.doesObjectExist(provider.getBucketName(), objectKey);
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "OSS 判断对象是否存在失败", ex);
        }
    }
}
