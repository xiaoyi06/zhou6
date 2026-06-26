package com.zhou6.cloud.message.vo;

/**
 * WebSocket 消息推送事件。
 */
public class MessagePushVO {

    private String eventType;
    private String unreadCount;
    private MessageVO message;

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getUnreadCount() { return unreadCount; }
    public void setUnreadCount(String unreadCount) { this.unreadCount = unreadCount; }
    public MessageVO getMessage() { return message; }
    public void setMessage(MessageVO message) { this.message = message; }
}
