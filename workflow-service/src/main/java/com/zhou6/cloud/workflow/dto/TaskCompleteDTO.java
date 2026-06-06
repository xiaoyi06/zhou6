package com.zhou6.cloud.workflow.dto;

import java.util.Map;

import lombok.Data;

/**
 * 流程任务办理请求参数。
 */
@Data
public class TaskCompleteDTO {

    /** Flowable 任务 ID。 */
    private String taskId;

    /** 流程变量。 */
    private Map<String, Object> variables;

    /** 审批意见。 */
    private String comment;
}
