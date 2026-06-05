package com.zhou6.cloud.file.storage.impl;

import java.io.InputStream;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.file.config.StorageProperties;
import com.zhou6.cloud.file.storage.StoredFile;

/**
 * 腾讯云 COS 文件存储实现，使用 COS 官方 SDK 上传、下载和删除对象。
 */
public class CosStorageServiceImpl extends AbstractFileStorageService {

    private final COSClient cosClient;

    /**
     * 创建 COS 存储服务。
     *
     * @param provider COS 连接配置
     */
    public CosStorageServiceImpl(StorageProperties.Provider provider) {
        super("cos", provider);
        requireText(provider.getRegion(), "对象存储 region 不能为空");
        COSCredentials credentials = new BasicCOSCredentials(provider.getAccessKey(), provider.getSecretKey());
        this.cosClient = new COSClient(credentials, new ClientConfig(new Region(provider.getRegion())));
    }

    /**
     * 使用 COS SDK 的 putObject 上传文件流。
     */
    @Override
    public StoredFile upload(InputStream inputStream, String fileName, String contentType, long fileSize) {
        String objectKey = buildObjectKey(fileName);
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileSize);
            metadata.setContentType(contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType);
            cosClient.putObject(new PutObjectRequest(provider.getBucketName(), objectKey, inputStream, metadata));
            return new StoredFile(objectKey, buildUrl(objectKey));
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "COS 文件上传失败", ex);
        }
    }

    /**
     * 使用 COS SDK 的 getObject 下载文件流。
     */
    @Override
    public InputStream download(String objectKey) {
        requireText(objectKey, "对象存储 objectKey 不能为空");
        try {
            COSObject object = cosClient.getObject(provider.getBucketName(), objectKey);
            return object.getObjectContent();
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "COS 文件下载失败", ex);
        }
    }

    /**
     * 使用 COS SDK 的 deleteObject 删除文件。
     */
    @Override
    public void delete(String objectKey) {
        requireText(objectKey, "对象存储 objectKey 不能为空");
        try {
            cosClient.deleteObject(provider.getBucketName(), objectKey);
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "COS 文件删除失败", ex);
        }
    }

    /**
     * 使用 COS SDK 的 doesObjectExist 判断对象是否存在。
     */
    @Override
    public boolean exists(String objectKey) {
        requireText(objectKey, "对象存储 objectKey 不能为空");
        try {
            return cosClient.doesObjectExist(provider.getBucketName(), objectKey);
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "COS 判断对象是否存在失败", ex);
        }
    }
}
