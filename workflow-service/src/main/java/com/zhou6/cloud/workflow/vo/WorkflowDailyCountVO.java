package com.zhou6.cloud.workflow.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工作流首页每日统计视图对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDailyCountVO {

    /** 日期，格式 yyyy-MM-dd。 */
    private String date;

    /** 当前用户待办数量。 */
    private String todoCount;

    /** 当前用户发起后待审核数量。 */
    private String pendingReviewCount;

    /** 当前用户已审核数量。 */
    private String reviewedCount;

    /** 当前用户发起后被驳回或废除的数量。 */
    private String rejectedCount;
}
