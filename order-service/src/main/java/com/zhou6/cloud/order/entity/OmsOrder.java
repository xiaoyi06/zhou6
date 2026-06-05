package com.zhou6.cloud.order.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 订单主表实体，对应 oms_order 表。
 */
@TableName("oms_order")
public class OmsOrder {

    /** 雪花算法主键 ID。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 订单唯一业务单号，也是跨服务协作的 bizId。 */
    private String orderSn;
    /** 下单用户 ID。 */
    private Long userId;
    /** 订单原始总金额。 */
    private BigDecimal totalAmount;
    /** 用户实际应支付现金金额。 */
    private BigDecimal payAmount;
    /** 订单状态：10-待支付，20-已支付，30-已取消，40-退款中，50-已退款。 */
    private Integer orderStatus;
    /** 创建人用户 ID。 */
    private Long createBy;
    /** 订单创建时间。 */
    private LocalDateTime createTime;
    /** 最后修改订单状态的操作人 ID。 */
    private Long updateBy;
    /** 订单最后更新时间。 */
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderSn() {
        return orderSn;
    }

    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
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

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
