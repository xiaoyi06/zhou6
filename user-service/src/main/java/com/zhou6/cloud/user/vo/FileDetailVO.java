package com.zhou6.cloud.user.vo;

/**
 * 文件详情响应对象，用于接收 file-service 返回的头像访问地址。
 */
public class FileDetailVO {

    /** 文件元数据 ID。 */
    private String id;

    /** 文件访问地址。 */
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
