package com.zhou6.cloud.community.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class PostVO {

    private String id;
    private String authorUserId;
    private String title;
    private String content;
    private String auditStatus;
    private String publishStatus;
    private Boolean top;
    private Boolean featured;
    private String viewCount;
    private String likeCount;
    private String commentCount;
    private String favoriteCount;
    private BigDecimal heatScore;
    private Boolean liked;
    private Boolean favorited;
    private List<PostResourceVO> resources;
    private List<TagVO> tags;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
