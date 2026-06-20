package com.zhou6.cloud.workflow.vo;

import lombok.Data;

/**
 * 流程模型详情响应对象，含 BPMN XML。
 */
@Data
public class ModelDetailVO {

    private String id;
    private String name;
    private String key;
    private String category;
    private Integer version;
    private String bpmnXml;
    private String createTime;
    private String lastUpdateTime;
    private String deploymentId;
}
