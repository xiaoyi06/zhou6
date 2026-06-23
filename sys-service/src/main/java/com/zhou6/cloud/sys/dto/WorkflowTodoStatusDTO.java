package com.zhou6.cloud.sys.dto;

/** 按流程来源完成或取消待办的幂等参数。 */
public class WorkflowTodoStatusDTO {

    private String userId;
    private String sourceId;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
}
