package com.zhou6.cloud.user.dto;

import lombok.Data;

/** 外部系统新增和修改请求参数。 */
@Data
public class ExternalSystemSaveDTO {

    private String id;
    private String systemName;
    /** 新增时必填且不允许修改。 */
    private String systemCode;
    private String systemUrl;
    private Integer sortOrder = 0;
    private String remark;
}
