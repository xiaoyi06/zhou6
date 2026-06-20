package com.zhou6.cloud.workflow.dto;

import lombok.Data;

/**
 * 流程模型分页查询请求参数。
 */
@Data
public class ModelQueryDTO {

    private String name;
    private String key;
    private String category;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
