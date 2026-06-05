package com.zhou6.cloud.account.dto;

import java.math.BigDecimal;

/**
 * 账户金额变更请求参数。
 */
public class AccountAmountDTO {

    /** 统一用户中心用户 ID。 */
    private Long userId;
    /** 本次变更金额。 */
    private BigDecimal amount;
    /** 外部业务类型，例如 ORDER_PAY、WITHDRAW、REWARD。 */
    private String bizType;
    /** 外部业务唯一号，例如订单号或活动奖励流水号。 */
    private String bizId;
    /** 触发本次变更的操作人 ID，系统自动操作时为 0。 */
    private Long operatorId;
    /** 业务备注。 */
    private String remark;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
