package com.zhou6.cloud.workflow.vo;

import lombok.Data;

/**
 * 流程模型响应对象。
 */
@Data
public class ModelVO {

    private String id;
    private String name;
    private String key;
    private String category;
    private Integer version;
    private String createTime;
    private String lastUpdateTime;
    private String deploymentId;
}
