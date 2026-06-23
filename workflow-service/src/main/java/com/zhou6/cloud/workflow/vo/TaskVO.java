package com.zhou6.cloud.workflow.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    /** 工作台状态，待办固定为 TODO。 */
    private String status;

    /** 流程定义名称。 */
    private String processDefinitionName;

    /** 发起人用户 ID。 */
    private String startUserId;

    private String startUserName;

    /** 流程发起时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 当前节点名称。 */
    private String currentTaskName;

    /** 任务创建时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
