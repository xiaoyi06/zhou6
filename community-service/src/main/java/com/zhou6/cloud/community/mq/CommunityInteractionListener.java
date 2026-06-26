package com.zhou6.cloud.community.mq;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.zhou6.cloud.community.service.CommunityInteractionPersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CommunityInteractionListener {

    private static final Logger log = LoggerFactory.getLogger(CommunityInteractionListener.class);

    private final ObjectMapper objectMapper;
    private final CommunityInteractionPersistenceService persistenceService;
    private final RabbitTemplate rabbitTemplate;
    private final int maxRedeliveryCount;

    public CommunityInteractionListener(ObjectMapper objectMapper,
            CommunityInteractionPersistenceService persistenceService, RabbitTemplate rabbitTemplate,
            @Value("${zhou6.community.mq.max-redelivery-count:3}") int maxRedeliveryCount) {
        this.objectMapper = objectMapper;
        this.persistenceService = persistenceService;
        this.rabbitTemplate = rabbitTemplate;
        this.maxRedeliveryCount = maxRedeliveryCount;
    }

    @RabbitListener(queues = "${zhou6.community.mq.interaction-queue:zhou6.community.interaction}",
            autoStartup = "${zhou6.community.mq.listener-auto-startup:true}", ackMode = "MANUAL")
    public void onInteraction(Message rawMessage, Channel channel) throws IOException {
        try {
            CommunityInteractionMessage message = objectMapper.readValue(rawMessage.getBody(), CommunityInteractionMessage.class);
            persistenceService.persist(message);
            channel.basicAck(rawMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception ex) {
            long retryCount = retryCount(rawMessage);
            if (retryCount < maxRedeliveryCount) {
                log.warn("Community interaction consume failed, requeue: retryCount={}", retryCount, ex);
                republishForRetry(rawMessage, retryCount + 1);
                channel.basicAck(rawMessage.getMessageProperties().getDeliveryTag(), false);
                return;
            }
            log.error("Community interaction consume failed and routed to dlq", ex);
            channel.basicNack(rawMessage.getMessageProperties().getDeliveryTag(), false, false);
        }
    }

    private long retryCount(Message rawMessage) {
        Object value = rawMessage.getMessageProperties().getHeaders().get("x-zhou6-retry-count");
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ex) {
                return maxRedeliveryCount;
            }
        }
        return 0L;
    }

    private void republishForRetry(Message rawMessage, long nextRetryCount) {
        String queue = rawMessage.getMessageProperties().getConsumerQueue();
        if (queue == null || queue.isBlank()) {
            throw new IllegalStateException("社区互动消息缺少消费队列，无法重试投递");
        }
        rawMessage.getMessageProperties().setHeader("x-zhou6-retry-count", nextRetryCount);
        rabbitTemplate.send("", queue, rawMessage);
    }
}
