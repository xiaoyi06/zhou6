package com.zhou6.cloud.order.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单详情响应。
 */
public class OrderVO {

    /** 订单唯一业务单号。 */
    private String orderSn;
    /** 下单用户 ID。 */
    private String userId;
    /** 订单原始总金额。 */
    private BigDecimal totalAmount;
    /** 用户实际应支付现金金额。 */
    private BigDecimal payAmount;
    /** 订单状态：10-待支付，20-已支付，30-已取消，40-退款中，50-已退款。 */
    private Integer orderStatus;
    /** 订单创建时间。 */
    private LocalDateTime createTime;

    public String getOrderSn() {
        return orderSn;
    }

    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
