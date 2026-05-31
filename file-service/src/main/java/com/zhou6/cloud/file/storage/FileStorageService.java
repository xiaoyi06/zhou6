package com.zhou6.cloud.file.storage;

import java.io.InputStream;

/**
 * 文件存储统一接口，屏蔽底层对象存储平台差异。
 */
public interface FileStorageService {

    /**
     * 上传文件。
     *
     * @param inputStream 文件输入流
     * @param fileName 原始文件名
     * @param contentType 文件 MIME 类型
     * @param fileSize 文件大小，单位字节
     * @return 上传后的对象键和访问地址
     */
    StoredFile upload(InputStream inputStream, String fileName, String contentType, long fileSize);

    /**
     * 下载文件。
     *
     * @param objectKey 对象存储中的唯一键
     * @return 文件输入流
     */
    InputStream download(String objectKey);

    /**
     * 删除文件。
     *
     * @param objectKey 对象存储中的唯一键
     */
    void delete(String objectKey);

    /**
     * 判断对象存储中指定对象是否存在。
     *
     * @param objectKey 对象存储中的唯一键
     * @return true 表示对象存在，false 表示对象不存在
     */
    boolean exists(String objectKey);

    /**
     * 获取当前实现支持的平台标识。
     *
     * @return 平台标识，如 minio、obs、oss、cos
     */
    String getPlatform();
}
