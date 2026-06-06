package com.zhou6.cloud.workflow.dto;

import java.util.Map;

import lombok.Data;

/**
 * 流程启动请求参数。
 */
@Data
public class ProcessStartDTO {

    /** 流程定义 Key。 */
    private String processKey;

    /** 业务唯一号，用于绑定订单、退款单等业务主键。 */
    private String businessKey;

    /** 流程变量。 */
    private Map<String, Object> variables;
}
