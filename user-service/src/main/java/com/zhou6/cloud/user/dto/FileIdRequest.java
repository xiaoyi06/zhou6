package com.zhou6.cloud.user.dto;

/**
 * 调用 file-service 时使用的文件 ID 请求参数。
 */
public class FileIdRequest {

    /** 文件元数据 ID。 */
    private String id;

    public FileIdRequest() {
    }

    public FileIdRequest(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
