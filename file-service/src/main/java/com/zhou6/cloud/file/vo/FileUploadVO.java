package com.zhou6.cloud.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 文件上传和详情接口响应对象。
 */
@Schema(description = "文件上传和详情接口响应对象")
public class FileUploadVO {

    /** 文件元数据 ID。 */
    @Schema(description = "文件元数据ID，字符串格式避免前端大整数精度丢失", example = "10001")
    private String id;
    /** 原始文件名。 */
    @Schema(description = "原始文件名", example = "avatar.png")
    private String fileName;
    /** 文件大小，单位字节。 */
    @Schema(description = "文件大小，单位字节", example = "102400")
    private String fileSize;
    /** 文件 MIME 类型。 */
    @Schema(description = "文件MIME类型", example = "image/png")
    private String contentType;
    /** 对象存储中的唯一键。 */
    @Schema(description = "对象存储中的唯一键", example = "2026/05/31/avatar.png")
    private String objectKey;
    /** 存储平台标识。 */
    @Schema(description = "存储平台标识", example = "minio")
    private String platform;
    /** 文件访问地址。 */
    @Schema(description = "文件访问地址", example = "https://example.com/files/avatar.png")
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

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
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
