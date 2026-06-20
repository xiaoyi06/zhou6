package com.zhou6.cloud.workflow.dto;

import lombok.Data;

/**
 * 流程定义分页查询请求参数。
 */
@Data
public class DefinitionQueryDTO {

    private String processKey;
    private String category;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
