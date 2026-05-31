package com.zhou6.cloud.common.dto;

import com.zhou6.cloud.common.handler.CommonResultCode;

/**
 * 统一接口响应体。
 *
 * @param <T> 响应数据类型
 */
public class R<T> {

    /** 业务响应码。 */
    private String code;

    /** 响应消息。 */
    private String message;

    /** 响应数据。 */
    private T data;

    /** 链路追踪 ID，预留给日志追踪使用。 */
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

    /**
     * 创建成功响应。
     *
     * @param data 响应数据
     * @param message 成功消息
     * @param <T> 响应数据类型
     * @return 成功响应体
     */
    public static <T> R<T> ok(T data, String message) {
        return new R<>(CommonResultCode.SUCCESS, message, data, null);
    }

    /**
     * 创建失败响应。
     *
     * @param code 业务错误码
     * @param message 错误消息
     * @param <T> 响应数据类型
     * @return 失败响应体
     */
    public static <T> R<T> fail(String code, String message) {
        return new R<>(code, message, null, null);
    }

    public static <T> R<T> fail(int code, String message) {
        return fail(String.valueOf(code), message);
    }

    /**
     * 判断响应是否成功。
     *
     * @return true 表示成功
     */
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
