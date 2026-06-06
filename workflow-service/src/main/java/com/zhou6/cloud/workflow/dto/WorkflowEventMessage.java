package com.zhou6.cloud.workflow.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 工作流状态变更事件消息。
 */
@Data
public class WorkflowEventMessage {

    /** 业务唯一号。 */
    private String businessKey;

    /** 状态编码，如 REJECTED、TERMINATED。 */
    private String status;

    /** 状态变更原因。 */
    private String reason;

    /** 事件发生时间。 */
    private LocalDateTime eventTime;
}
