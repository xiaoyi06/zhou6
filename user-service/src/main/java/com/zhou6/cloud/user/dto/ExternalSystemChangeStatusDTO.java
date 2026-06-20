package com.zhou6.cloud.user.dto;

import lombok.Data;

/** 外部系统启停请求参数。 */
@Data
public class ExternalSystemChangeStatusDTO {
    private String id;
    private Integer status;
}
