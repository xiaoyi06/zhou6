package com.zhou6.cloud.workflow.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 已办任务视图对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricTaskVO {

    /** Flowable 历史任务 ID。 */
    private String taskId;

    /** 任务名称。 */
    private String taskName;

    /** 流程实例 ID。 */
    private String processInstanceId;

    /** 业务唯一号。 */
    private String businessKey;

    /** 任务结束时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
}
