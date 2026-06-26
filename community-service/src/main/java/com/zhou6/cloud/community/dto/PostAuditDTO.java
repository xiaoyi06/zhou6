package com.zhou6.cloud.community.dto;

import lombok.Data;

@Data
public class PostAuditDTO {

    private String id;
    private String auditStatus;
    private String publishStatus;
}
