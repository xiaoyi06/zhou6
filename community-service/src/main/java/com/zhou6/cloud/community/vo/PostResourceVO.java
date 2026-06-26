package com.zhou6.cloud.community.vo;

import lombok.Data;

@Data
public class PostResourceVO {

    private String id;
    private String postId;
    private String fileId;
    private String resourceType;
    private Integer sortOrder;
}
