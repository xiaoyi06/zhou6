package com.zhou6.cloud.account.handler;

import com.zhou6.cloud.common.handler.ErrorCode;

/**
 * 账户服务业务错误码。
 */
public enum AccountErrorCode implements ErrorCode {

    ACCOUNT_NOT_FOUND("060001", 404, "现金账户不存在"),
    BALANCE_NOT_ENOUGH("060002", 400, "可用余额不足"),
    FROZEN_BALANCE_NOT_ENOUGH("060003", 400, "冻结余额不足"),
    AMOUNT_INVALID("060004", 400, "金额必须大于0"),
    BIZ_ID_DUPLICATED("060005", 409, "业务流水已处理"),
    ORIGINAL_FLOW_NOT_FOUND("060006", 404, "原始流水不存在"),
    BIZ_PARAM_INVALID("060007", 400, "业务类型或业务唯一号不正确"),
    ACCOUNT_CACHE_ERROR("060008", 500, "账户缓存操作失败"),
    ACCOUNT_CONSISTENCY_ERROR("060009", 500, "账户数据一致性异常");

    private final String code;
    private final int httpStatus;
    private final String message;

    AccountErrorCode(String code, int httpStatus, String message) {
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
