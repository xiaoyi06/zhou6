package com.zhou6.cloud.community.vo;

import lombok.Data;

@Data
public class MentionCandidateVO {

    private String userId;
    private String username;
    private String nickname;
    private Boolean followed;
    private Boolean favoriteUser;
    private Integer priority;
}
