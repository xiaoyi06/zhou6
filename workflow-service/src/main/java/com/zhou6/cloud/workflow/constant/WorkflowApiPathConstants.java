package com.zhou6.cloud.workflow.constant;

import com.zhou6.cloud.common.constant.ApiPathConstants;

/**
 * workflow-service API 路径常量。
 */
public final class WorkflowApiPathConstants {

    /** workflow-service 对外接口根路径。 */
    public static final String WORKFLOW = ApiPathConstants.API_V1 + "/workflow-api";

    /** workflow-service 内部 RPC 接口根路径。 */
    public static final String INNER = WORKFLOW + "/inner";

    private WorkflowApiPathConstants() {
    }
}
