package com.zhou6.cloud.message.websocket;

import com.zhou6.cloud.message.vo.MessagePushVO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * 消息 WebSocket 推送器。
 */
@Component
public class MessageWebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public MessageWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void pushToUser(String userId, MessagePushVO push) {
        messagingTemplate.convertAndSendToUser(userId, "/queue/messages", push);
    }
}
