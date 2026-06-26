package com.zhou6.cloud.community.dto;

import java.util.List;

import lombok.Data;

@Data
public class PostSaveDTO {

    private String title;
    private String content;
    private List<PostResourceDTO> resources;
    private List<String> mentionUserIds;
    private List<String> tagIds;
}
