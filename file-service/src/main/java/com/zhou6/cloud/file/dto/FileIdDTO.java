package com.zhou6.cloud.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 文件 ID 请求参数。
 */
@Schema(description = "文件ID请求参数")
public class FileIdDTO {

    /** 文件元数据 ID。 */
    @Schema(description = "文件元数据ID，字符串格式避免前端大整数精度丢失", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
