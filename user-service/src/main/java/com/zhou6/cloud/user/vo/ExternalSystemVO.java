package com.zhou6.cloud.user.vo;

import lombok.Data;

/** 外部系统响应对象，同时作为角色所属系统下拉选项。 */
@Data
public class ExternalSystemVO {
    private String id;
    private String systemName;
    private String systemCode;
    private String systemUrl;
    private Integer sortOrder;
    private Integer status;
    private String remark;
}
