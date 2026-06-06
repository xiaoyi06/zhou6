package com.zhou6.cloud.workflow.handler;

import com.zhou6.cloud.common.handler.ErrorCode;

/**
 * 工作流服务业务错误码。
 */
public enum WorkflowErrorCode implements ErrorCode {

    BIZ_PARAM_INVALID("080001", 400, "工作流请求参数不正确"),
    PROCESS_INSTANCE_NOT_FOUND("080002", 404, "流程实例不存在"),
    TASK_NOT_FOUND("080003", 404, "流程任务不存在"),
    PROCESS_OPERATION_FAILED("080004", 500, "流程操作失败");

    private final String code;
    private final int httpStatus;
    private final String message;

    WorkflowErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
