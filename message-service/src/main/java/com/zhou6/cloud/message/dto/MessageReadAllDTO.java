package com.zhou6.cloud.message.dto;

/**
 * 全部已读参数。
 */
public class MessageReadAllDTO {

    private String channel;
    private String messageType;

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
}
