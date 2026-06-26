package com.zhou6.cloud.community.dto;

import java.util.List;

import lombok.Data;

@Data
public class TagReserveDTO {

    private String postId;
    private List<String> tagIds;
}
