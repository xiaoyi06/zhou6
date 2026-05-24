package com.zhou6.cloud.common.handler;

public class BizException extends RuntimeException {

    private final int code;
    private final int httpStatus;

    public BizException(int code, String message) {
        this(code, code, message);
    }

    public BizException(int code, int httpStatus, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public int getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
