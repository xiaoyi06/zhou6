package com.zhou6.cloud.workflow.vo;

import lombok.Data;

/**
 * 部署结果响应对象。
 */
@Data
public class DeployResultVO {

    private String deploymentId;
    private String definitionId;
    private String definitionKey;
    private Integer version;
}
