package com.zhou6.cloud.order.dto;

import java.math.BigDecimal;

/**
 * 创建并支付订单请求参数。
 */
public class OrderCreateDTO {

    /** 订单原始总金额。 */
    private BigDecimal totalAmount;
    /** 用户实际应支付现金金额。 */
    private BigDecimal payAmount;

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }
}
