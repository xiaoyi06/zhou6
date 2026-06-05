package com.zhou6.cloud.order.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 订单现金扣减凭证实体，对应 oms_order_pay_receipt 表。
 */
@TableName("oms_order_pay_receipt")
public class OmsOrderPayReceipt {

    /** 雪花算法主键 ID。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 订单唯一业务单号。 */
    private String orderSn;
    /** 下单用户 ID。 */
    private Long userId;
    /** 已在账户系统成功预冻结的现金金额。 */
    private BigDecimal freezeAmount;
    /** 凭证状态：1-已预冻结，2-已确认扣除，3-已反向释放。 */
    private Integer receiptStatus;
    /** 凭证创建人用户 ID。 */
    private Long createBy;
    /** 凭证创建时间。 */
    private LocalDateTime createTime;
    /** 最后修改凭证状态的操作人 ID。 */
    private Long updateBy;
    /** 凭证最后更新时间。 */
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

    public BigDecimal getFreezeAmount() {
        return freezeAmount;
    }

    public void setFreezeAmount(BigDecimal freezeAmount) {
        this.freezeAmount = freezeAmount;
    }

    public Integer getReceiptStatus() {
        return receiptStatus;
    }

    public void setReceiptStatus(Integer receiptStatus) {
        this.receiptStatus = receiptStatus;
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
