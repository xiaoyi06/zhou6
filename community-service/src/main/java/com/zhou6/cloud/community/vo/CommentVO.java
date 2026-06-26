package com.zhou6.cloud.community.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class CommentVO {

    private String id;
    private String postId;
    private String parentId;
    private String rootId;
    private String authorUserId;
    private String replyToUserId;
    private String content;
    private String likeCount;
    private Boolean liked;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private List<CommentVO> children = new ArrayList<>();
}
