package com.zhou6.cloud.file.dto;

/**
 * 文件上传和详情接口响应对象。
 */
public class FileUploadVO {

    /** 文件元数据 ID。 */
    private String id;
    /** 原始文件名。 */
    private String fileName;
    /** 文件大小，单位字节。 */
    private Long fileSize;
    /** 文件 MIME 类型。 */
    private String contentType;
    /** 对象存储中的唯一键。 */
    private String objectKey;
    /** 存储平台标识。 */
    private String platform;
    /** 文件访问地址。 */
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
