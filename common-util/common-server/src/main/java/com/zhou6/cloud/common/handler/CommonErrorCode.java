package com.zhou6.cloud.common.handler;

public enum CommonErrorCode implements ErrorCode {

    PARAM_INVALID("040000", 400, "请求参数不正确"),
    TOKEN_INVALID("040100", 401, "访问令牌无效或已过期，请重新登录"),
    LOGIN_FAILED("040101", 401, "账号或密码错误"),
    REFRESH_TOKEN_INVALID("040102", 401, "刷新令牌无效或已过期"),
    LOGIN_SESSION_EXPIRED("040103", 401, "当前登录已失效，请重新登录"),
    REFRESH_TOKEN_DATA_INVALID("040104", 401, "刷新令牌数据无效"),
    ACCOUNT_DISABLED("040105", 401, "你的账号已被停用"),
    ACCOUNT_LOCKED("040106", 401, "你的账号被锁定，请稍后重试"),
    FORBIDDEN("040300", 403, "没有权限访问该接口"),
    USER_NOT_FOUND("040401", 404, "用户信息不存在"),
    SYSTEM_ERROR("050000", 500, "系统繁忙，请稍后再试");

    private final String code;
    private final int httpStatus;
    private final String message;

    CommonErrorCode(String code, int httpStatus, String message) {
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
