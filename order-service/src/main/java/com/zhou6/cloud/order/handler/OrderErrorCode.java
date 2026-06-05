package com.zhou6.cloud.order.handler;

import com.zhou6.cloud.common.handler.ErrorCode;

/**
 * 订单服务业务错误码。
 */
public enum OrderErrorCode implements ErrorCode {

    PARAM_INVALID("070001", 400, "订单参数不正确"),
    AMOUNT_INVALID("070002", 400, "订单金额必须大于0"),
    ORDER_NOT_FOUND("070003", 404, "订单不存在"),
    ORDER_STATUS_INVALID("070004", 409, "订单状态不允许当前操作"),
    ORDER_SN_DUPLICATED("070005", 409, "订单编号已存在"),
    RECEIPT_NOT_FOUND("070006", 404, "订单扣款凭证不存在"),
    ACCOUNT_FREEZE_FAILED("070007", 502, "账户预冻结失败"),
    ACCOUNT_REVERSE_FAILED("070008", 502, "账户冲正失败");

    private final String code;
    private final int httpStatus;
    private final String message;

    OrderErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    /**
     * 获取业务错误码。
     *
     * @return 业务错误码
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 获取建议返回的 HTTP 状态码。
     *
     * @return HTTP 状态码
     */
    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * 获取默认错误消息。
     *
     * @return 默认错误消息
     */
    @Override
    public String getMessage() {
        return message;
    }
}
