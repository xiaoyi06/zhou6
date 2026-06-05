package com.zhou6.cloud.order.dto;

/**
 * 退款审批请求参数。
 */
public class OrderRefundDTO {

    /** 需要退款审批的订单唯一业务单号。 */
    private String orderSn;

    public String getOrderSn() {
        return orderSn;
    }

    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }
}
