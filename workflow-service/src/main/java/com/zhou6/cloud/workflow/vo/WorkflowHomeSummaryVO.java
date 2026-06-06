package com.zhou6.cloud.workflow.vo;

import java.util.List;

import lombok.Data;

/**
 * 工作流首页统计视图对象。
 */
@Data
public class WorkflowHomeSummaryVO {

    /** 当前用户待办数量。 */
    private String todoCount;

    /** 当前用户发起后待审核数量。 */
    private String pendingReviewCount;

    /** 当前用户已审核数量。 */
    private String reviewedCount;

    /** 当前用户发起后被驳回或废除的数量。 */
    private String rejectedCount;

    /** 最近 3 天折线图统计数据。 */
    private List<WorkflowDailyCountVO> trend;
}
