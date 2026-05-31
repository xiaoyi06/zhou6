package com.zhou6.cloud.file.storage.impl;

import java.io.InputStream;

import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.file.config.StorageProperties;
import com.zhou6.cloud.file.storage.StoredFile;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

/**
 * MinIO 文件存储实现，使用 MinIO 官方 SDK 上传、下载和删除对象。
 */
public class MinioStorageServiceImpl extends AbstractFileStorageService {

    private final MinioClient minioClient;

    /**
     * 创建 MinIO 存储服务。
     *
     * @param provider MinIO 连接配置
     */
    public MinioStorageServiceImpl(StorageProperties.Provider provider) {
        super("minio", provider);
        this.minioClient = MinioClient.builder()
                .endpoint(provider.getEndpoint())
                .credentials(provider.getAccessKey(), provider.getSecretKey())
                .build();
    }

    /**
     * 使用 MinIO SDK 的 putObject 上传文件流。
     */
    @Override
    public StoredFile upload(InputStream inputStream, String fileName, String contentType, long fileSize) {
        String objectKey = buildObjectKey(fileName);
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(provider.getBucketName())
                    .object(objectKey)
                    .stream(inputStream, fileSize, -1)
                    .contentType(contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType)
                    .build());
            return new StoredFile(objectKey, buildUrl(objectKey));
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "MinIO 文件上传失败", ex);
        }
    }

    /**
     * 使用 MinIO SDK 的 getObject 下载文件流。
     */
    @Override
    public InputStream download(String objectKey) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(provider.getBucketName())
                    .object(objectKey)
                    .build());
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "MinIO 文件下载失败", ex);
        }
    }

    /**
     * 使用 MinIO SDK 的 removeObject 删除文件。
     */
    @Override
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(provider.getBucketName())
                    .object(objectKey)
                    .build());
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "MinIO 文件删除失败", ex);
        }
    }

    /**
     * 使用 MinIO SDK 的 statObject 判断对象是否存在。
     */
    @Override
    public boolean exists(String objectKey) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(provider.getBucketName())
                    .object(objectKey)
                    .build());
            return true;
        } catch (ErrorResponseException ex) {
            String code = ex.errorResponse() == null ? null : ex.errorResponse().code();
            if ("NoSuchKey".equals(code) || "NoSuchObject".equals(code) || "NoSuchBucket".equals(code)) {
                return false;
            }
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "MinIO 判断对象是否存在失败", ex);
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "MinIO 判断对象是否存在失败", ex);
        }
    }
}
