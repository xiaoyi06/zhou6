package com.zhou6.cloud.file.dto;

import java.io.InputStream;

/**
 * 文件下载结果，供 Controller 组装二进制响应。
 */
public class DownloadFile {

    /** 原始文件名。 */
    private final String fileName;
    /** 文件大小，单位字节。 */
    private final Long fileSize;
    /** 文件 MIME 类型。 */
    private final String contentType;
    /** 文件输入流。 */
    private final InputStream inputStream;

    public DownloadFile(String fileName, Long fileSize, String contentType, InputStream inputStream) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.inputStream = inputStream;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
