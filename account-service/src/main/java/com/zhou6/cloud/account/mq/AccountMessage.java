package com.zhou6.cloud.account.mq;

import java.math.BigDecimal;

/**
 * 账户 MQ 标准消息体。
 *
 * <p>异步消费者无法从前端 Token 获取用户上下文，因此上游消息必须显式携带 operatorId。</p>
 */
public class AccountMessage {

    /** 统一用户中心用户 ID。 */
    private Long userId;
    /** 本次账户变更金额。 */
    private BigDecimal amount;
    /** 外部业务类型。 */
    private String bizType;
    /** 外部业务唯一号。 */
    private String bizId;
    /** 触发本次变更的操作人 ID。 */
    private Long operatorId;
    /** 消息备注。 */
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
