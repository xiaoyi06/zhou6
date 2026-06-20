package com.zhou6.cloud.user.dto;

import lombok.Data;

/** 外部系统分页查询参数。 */
@Data
public class ExternalSystemQueryDTO {

    private String systemName;
    private String systemCode;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
