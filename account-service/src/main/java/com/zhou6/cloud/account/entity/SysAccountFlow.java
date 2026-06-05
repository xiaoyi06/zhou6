package com.zhou6.cloud.account.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 现金变动流水实体，对应 sys_account_flow 表。
 *
 * <p>流水是只增不改的审计凭证，因此只保留创建人和创建时间。</p>
 */
@TableName("sys_account_flow")
public class SysAccountFlow {

    /** 流水号主键，雪花 ID。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 统一用户中心用户 ID。 */
    private Long userId;
    /** 流水方向：1-入账，2-出账。 */
    private Short direction;
    /** 账户域：1-可用域，2-冻结域，3-待结算域。 */
    private Short targetType;
    /** 流水金额。 */
    private BigDecimal amount;
    /** 外部业务类型。 */
    private String bizType;
    /** 外部业务唯一号。 */
    private String bizId;
    /** 流水备注。 */
    private String remark;
    /** 触发账目变动的操作人。 */
    private Long createBy;
    /** 实际记账落盘时间。 */
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Short getDirection() {
        return direction;
    }

    public void setDirection(Short direction) {
        this.direction = direction;
    }

    public Short getTargetType() {
        return targetType;
    }

    public void setTargetType(Short targetType) {
        this.targetType = targetType;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
}
