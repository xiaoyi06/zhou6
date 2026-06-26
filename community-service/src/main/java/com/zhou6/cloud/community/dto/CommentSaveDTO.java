package com.zhou6.cloud.community.dto;

import java.util.List;

import lombok.Data;

@Data
public class CommentSaveDTO {

    private String postId;
    private String parentId;
    private String replyToUserId;
    private String content;
    private List<String> mentionUserIds;
}
