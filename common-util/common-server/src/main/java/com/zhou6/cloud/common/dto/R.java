package com.zhou6.cloud.common.dto;

import com.zhou6.cloud.common.handler.CommonResultCode;

public class R<T> {

    private String code;

    private String message;

    private T data;
    private String traceId;

    public R() {
    }

    private R(String code, String message, T data, String traceId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = traceId;
    }

    public static <T> R<T> ok(T data) {
        return ok(data, CommonResultCode.SUCCESS_MESSAGE);
    }

    public static <T> R<T> ok(T data, String message) {
        return new R<>(CommonResultCode.SUCCESS, message, data, null);
    }

    public static <T> R<T> fail(String code, String message) {
        return new R<>(code, message, null, null);
    }

    public static <T> R<T> fail(int code, String message) {
        return fail(String.valueOf(code), message);
    }

    public boolean success() {
        return CommonResultCode.SUCCESS.equals(code);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
