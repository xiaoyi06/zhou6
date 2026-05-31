package com.zhou6.cloud.common.handler;

public class BizException extends RuntimeException {

    private final String code;
    private final int httpStatus;

    public BizException(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getHttpStatus(), errorCode.getMessage());
    }

    public BizException(ErrorCode errorCode, String message) {
        this(errorCode.getCode(), errorCode.getHttpStatus(), message);
    }

    public BizException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode.getCode(), errorCode.getHttpStatus(), message, cause);
    }

    public BizException(int code, String message) {
        this(code, code, message);
    }

    public BizException(int code, int httpStatus, String message) {
        this(String.valueOf(code), httpStatus, message);
    }

    public BizException(int code, int httpStatus, String message, Throwable cause) {
        this(String.valueOf(code), httpStatus, message, cause);
    }

    public BizException(String code, int httpStatus, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public BizException(String code, int httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
