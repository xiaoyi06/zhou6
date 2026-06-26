package com.zhou6.cloud.message.dto;

import java.util.List;

/**
 * 内部发送消息参数。
 */
public class InternalMessageSendDTO {

    private String channel;
    private String messageType;
    private String title;
    private String content;
    private String sourceType;
    private String sourceName;
    private String sourceId;
    private String businessType;
    private String businessId;
    private List<String> receiverUserIds;
    private String linkType;
    private String linkUrl;
    private String linkParams;

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    public List<String> getReceiverUserIds() { return receiverUserIds; }
    public void setReceiverUserIds(List<String> receiverUserIds) { this.receiverUserIds = receiverUserIds; }
    public String getLinkType() { return linkType; }
    public void setLinkType(String linkType) { this.linkType = linkType; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public String getLinkParams() { return linkParams; }
    public void setLinkParams(String linkParams) { this.linkParams = linkParams; }
}
