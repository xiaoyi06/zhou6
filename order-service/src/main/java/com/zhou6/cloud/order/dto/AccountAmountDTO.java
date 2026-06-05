package com.zhou6.cloud.order.dto;

import java.math.BigDecimal;

/**
 * 调用账户服务的金额变更请求。
 */
public class AccountAmountDTO {

    /** 统一用户中心用户 ID。 */
    private Long userId;
    /** 本次账户变更金额。 */
    private BigDecimal amount;
    /** 外部业务类型，例如 ORDER_PAY。 */
    private String bizType;
    /** 外部业务唯一号，例如订单号。 */
    private String bizId;
    /** 触发本次账户变更的操作人 ID。 */
    private Long operatorId;
    /** 账户流水备注。 */
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
