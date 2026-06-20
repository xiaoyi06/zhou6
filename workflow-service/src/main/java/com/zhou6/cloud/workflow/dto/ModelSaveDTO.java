package com.zhou6.cloud.workflow.dto;

import lombok.Data;

/**
 * 流程模型保存请求参数。
 */
@Data
public class ModelSaveDTO {

    /** 模型 ID，新增时为空，修改时必填。 */
    private String id;

    /** 模型名称。 */
    private String name;

    /** 模型 Key，新增时必填。 */
    private String key;

    /** 模型分类。 */
    private String category;

    /** BPMN 2.0 XML，前端设计器绘制后提交。 */
    private String bpmnXml;
}
