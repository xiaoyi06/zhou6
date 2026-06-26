package com.zhou6.cloud.community.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhou6.cloud.community.service.CommunityInteractionCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CommunityInteractionPublisher {

    private static final Logger log = LoggerFactory.getLogger(CommunityInteractionPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final CommunityInteractionCacheService cacheService;
    private final ObjectMapper objectMapper;
    private final String exchange;
    private final String routingKey;
    private final int compensateBatchSize;

    public CommunityInteractionPublisher(RabbitTemplate rabbitTemplate,
            CommunityInteractionCacheService cacheService,
            ObjectMapper objectMapper,
            @Value("${zhou6.community.mq.exchange:zhou6.community.interaction}") String exchange,
            @Value("${zhou6.community.mq.interaction-routing-key:zhou6.community.interaction}") String routingKey,
            @Value("${zhou6.community.mq.compensate-batch-size:100}") int compensateBatchSize) {
        this.rabbitTemplate = rabbitTemplate;
        this.cacheService = cacheService;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.compensateBatchSize = compensateBatchSize;
    }

    public void publish(CommunityInteractionMessage message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
        } catch (Exception ex) {
            log.warn("Publish community interaction failed, keep pending event for compensation: eventId={}",
                    message == null ? null : message.getEventId(), ex);
        }
    }

    @Scheduled(fixedDelayString = "${zhou6.community.mq.compensate-delay-ms:10000}")
    public void compensatePendingEvents() {
        for (String eventJson : cacheService.pendingEvents(compensateBatchSize)) {
            try {
                publish(objectMapper.readValue(eventJson, CommunityInteractionMessage.class));
            } catch (Exception ex) {
                log.warn("Publish community pending interaction failed: pendingKey={}", cacheService.pendingKey(), ex);
                return;
            }
        }
    }

    public String toEventJson(CommunityInteractionMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("序列化社区互动事件失败", ex);
        }
    }
}
