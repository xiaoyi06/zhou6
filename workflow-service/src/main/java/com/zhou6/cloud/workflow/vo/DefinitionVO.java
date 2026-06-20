package com.zhou6.cloud.workflow.vo;

import lombok.Data;

/**
 * 流程定义响应对象。
 */
@Data
public class DefinitionVO {

    private String id;
    private String key;
    private String name;
    private String category;
    private Integer version;
    private String deploymentId;
    private Boolean suspended;
    private String tenantId;
}
