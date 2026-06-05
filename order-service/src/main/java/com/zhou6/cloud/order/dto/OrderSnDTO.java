package com.zhou6.cloud.order.dto;

/**
 * 订单号请求参数。
 */
public class OrderSnDTO {

    /** 订单唯一业务单号。 */
    private String orderSn;

    public String getOrderSn() {
        return orderSn;
    }

    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }
}
