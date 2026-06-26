package com.zhou6.cloud.sys.service;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.zhou6.cloud.sys.entity.SysTodo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 消息中心内部调用客户端。
 */
@Component
public class MessageCenterClient {

    private static final Logger log = LoggerFactory.getLogger(MessageCenterClient.class);
    private static final String MESSAGE_SERVICE = "message-service";
    private static final String SEND_PATH = "/api/v1/message-api/internal/message/send";

    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;
    private final String configuredBaseUrl;

    public MessageCenterClient(DiscoveryClient discoveryClient, RestTemplateBuilder restTemplateBuilder,
            @Value("${zhou6.message.base-url:}") String configuredBaseUrl) {
        this.discoveryClient = discoveryClient;
        this.restTemplate = restTemplateBuilder.build();
        this.configuredBaseUrl = configuredBaseUrl;
    }

    public void sendTodoReminder(SysTodo todo) {
        if (todo == null || todo.getUserId() == null || todo.getId() == null) {
            return;
        }
        URI baseUri = resolveBaseUri();
        if (baseUri == null) {
            log.warn("Skip todo reminder message because message-service is unavailable, todoId={}", todo.getId());
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("channel", "INTERNAL");
        payload.put("messageType", "TODO");
        payload.put("title", "待办提醒：" + todo.getTitle());
        payload.put("content", todo.getContent() == null || todo.getContent().isBlank()
                ? "您有一个待办已到提醒时间，请及时查看。"
                : todo.getContent());
        payload.put("sourceType", "TODO");
        payload.put("sourceName", "待办中心");
        payload.put("sourceId", String.valueOf(todo.getId()));
        payload.put("businessType", todo.getSourceBusinessType() == null ? "TODO" : todo.getSourceBusinessType());
        payload.put("businessId", todo.getSourceBusinessId() == null
                ? String.valueOf(todo.getId()) : todo.getSourceBusinessId());
        payload.put("receiverUserIds", List.of(String.valueOf(todo.getUserId())));
        payload.put("linkType", "ROUTE");
        payload.put("linkUrl", "/todo");
        try {
            restTemplate.postForObject(baseUri.resolve(SEND_PATH), payload, Object.class);
        } catch (Exception ex) {
            log.warn("Failed to send todo reminder message, todoId={}", todo.getId(), ex);
        }
    }

    private URI resolveBaseUri() {
        if (configuredBaseUrl != null && !configuredBaseUrl.isBlank()) {
            return URI.create(trimTrailingSlash(configuredBaseUrl.trim()));
        }
        List<ServiceInstance> instances = discoveryClient.getInstances(MESSAGE_SERVICE);
        if (instances == null || instances.isEmpty()) {
            return null;
        }
        return instances.get(0).getUri();
    }

    private String trimTrailingSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
