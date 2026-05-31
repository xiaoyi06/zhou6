package com.zhou6.cloud.file.storage;

/**
 * 对象存储上传结果。
 */
public class StoredFile {

    /** 对象存储中的唯一键。 */
    private final String objectKey;
    /** 文件访问地址。 */
    private final String url;

    public StoredFile(String objectKey, String url) {
        this.objectKey = objectKey;
        this.url = url;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public String getUrl() {
        return url;
    }
}
