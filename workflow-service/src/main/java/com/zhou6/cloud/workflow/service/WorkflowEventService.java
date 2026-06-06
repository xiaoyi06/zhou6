package com.zhou6.cloud.workflow.service;

/**
 * 工作流状态事件服务。
 */
public interface WorkflowEventService {

    /**
     * 发布工作流状态变更事件。
     *
     * @param businessKey 业务唯一号
     * @param status 状态编码
     * @param reason 状态变更原因
     */
    void publishStatusChange(String businessKey, String status, String reason);
}
