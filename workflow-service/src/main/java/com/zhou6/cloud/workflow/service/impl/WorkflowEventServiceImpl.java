package com.zhou6.cloud.workflow.service.impl;

import java.time.LocalDateTime;

import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.workflow.dto.WorkflowEventMessage;
import com.zhou6.cloud.workflow.handler.WorkflowErrorCode;
import com.zhou6.cloud.workflow.service.WorkflowEventService;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 工作流状态事件 RabbitMQ 发布实现。
 */
@Service
public class WorkflowEventServiceImpl implements WorkflowEventService {

    private final RabbitTemplate rabbitTemplate;
    private final String eventExchange;
    private final String statusChangeRoutingKey;

    public WorkflowEventServiceImpl(RabbitTemplate rabbitTemplate,
            @Value("${zhou6.workflow.mq.event-exchange:zhou6.workflow.event.exchange}") String eventExchange,
            @Value("${zhou6.workflow.mq.status-change-routing-key:zhou6.workflow.event.status-change}")
                    String statusChangeRoutingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.eventExchange = eventExchange;
        this.statusChangeRoutingKey = statusChangeRoutingKey;
    }

    @Override
    public void publishStatusChange(String businessKey, String status, String reason) {
        WorkflowEventMessage message = new WorkflowEventMessage();
        message.setBusinessKey(businessKey);
        message.setStatus(status);
        message.setReason(reason);
        message.setEventTime(LocalDateTime.now());
        try {
            rabbitTemplate.convertAndSend(eventExchange, statusChangeRoutingKey, message);
        } catch (AmqpException ex) {
            throw new BizException(WorkflowErrorCode.PROCESS_OPERATION_FAILED, "工作流状态事件发送失败", ex);
        }
    }
}
