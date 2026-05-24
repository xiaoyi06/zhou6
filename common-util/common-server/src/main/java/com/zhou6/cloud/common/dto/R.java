package com.zhou6.cloud.common.dto;

public class R<T> {

    private int code;
    private String msg;
    private T data;
    private String traceId;

    public R() {
    }

    private R(int code, String msg, T data, String traceId) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.traceId = traceId;
    }

    public static <T> R<T> ok(T data) {
        return new R<>(0, "success", data, null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null, null);
    }

    public boolean success() {
        return code == 0;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
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
