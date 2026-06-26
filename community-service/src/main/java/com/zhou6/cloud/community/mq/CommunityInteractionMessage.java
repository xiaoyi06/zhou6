package com.zhou6.cloud.community.mq;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CommunityInteractionMessage {

    private String eventId;
    private String action;
    private String targetType;
    private Long targetId;
    private Long userId;
    private LocalDateTime eventTime;
}
