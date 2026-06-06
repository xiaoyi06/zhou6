package com.zhou6.cloud.workflow.vo;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 待办任务视图对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskVO {

    /** Flowable 任务 ID。 */
    private String taskId;

    /** 任务名称。 */
    private String taskName;

    /** 流程实例 ID。 */
    private String processInstanceId;

    /** 业务唯一号。 */
    private String businessKey;

    /** 任务创建时间。 */
    private Date createTime;
}
