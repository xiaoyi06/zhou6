package com.zhou6.cloud.file.storage.impl;

import java.io.InputStream;

import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.ObsObject;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.PutObjectRequest;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.file.config.StorageProperties;
import com.zhou6.cloud.file.storage.StoredFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 华为云 OBS 文件存储实现，使用 OBS 官方 SDK 上传、下载和删除对象。
 */
public class ObsStorageServiceImpl extends AbstractFileStorageService {

    private final ObsClient obsClient;

    /**
     * 创建 OBS 存储服务。
     *
     * @param provider OBS 连接配置
     */
    public ObsStorageServiceImpl(StorageProperties.Provider provider) {
        super("obs", provider);
        this.obsClient = new ObsClient(provider.getAccessKey(), provider.getSecretKey(), provider.getEndpoint());
    }

    /**
     * 使用 OBS SDK 的 putObject 上传文件流。
     */
    @Override
    public StoredFile upload(InputStream inputStream, String fileName, String contentType, long fileSize) {
        String objectKey = buildObjectKey(fileName);
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileSize);
            metadata.setContentType(contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType);
            PutObjectRequest request = new PutObjectRequest(provider.getBucketName(), objectKey, inputStream);
            request.setMetadata(metadata);
            obsClient.putObject(request);
            return new StoredFile(objectKey, buildUrl(objectKey));
        } catch (ObsException ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "OBS 文件上传失败", ex);
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "OBS 文件上传失败", ex);
        }
    }

    /**
     * 使用 OBS SDK 的 getObject 下载文件流。
     */
    @Override
    public InputStream download(String objectKey) {
        requireText(objectKey, "对象存储 objectKey 不能为空");
        try {
            ObsObject object = obsClient.getObject(provider.getBucketName(), objectKey);
            return object.getObjectContent();
        } catch (ObsException ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "OBS 文件下载失败", ex);
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "OBS 文件下载失败", ex);
        }
    }

    /**
     * 使用 OBS SDK 的 deleteObject 删除文件。
     */
    @Override
    public void delete(String objectKey) {
        requireText(objectKey, "对象存储 objectKey 不能为空");
        try {
            obsClient.deleteObject(provider.getBucketName(), objectKey);
        } catch (ObsException ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "OBS 文件删除失败", ex);
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "OBS 文件删除失败", ex);
        }
    }

    /**
     * 使用 OBS SDK 的 doesObjectExist 判断对象是否存在。
     */
    @Override
    public boolean exists(String objectKey) {
        requireText(objectKey, "对象存储 objectKey 不能为空");
        try {
            return obsClient.doesObjectExist(provider.getBucketName(), objectKey);
        } catch (ObsException ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "OBS 判断对象是否存在失败", ex);
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "OBS 判断对象是否存在失败", ex);
        }
    }

}
