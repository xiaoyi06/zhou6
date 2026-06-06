package com.zhou6.cloud.workflow.dto;

import lombok.Data;

/**
 * 流程终止请求参数。
 */
@Data
public class ProcessTerminateDTO {

    /** 业务唯一号。 */
    private String businessKey;

    /** 终止原因。 */
    private String reason;
}
