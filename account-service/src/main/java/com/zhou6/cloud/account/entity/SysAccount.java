package com.zhou6.cloud.account.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户现金余额账户快照实体，对应 sys_account 表。
 */
@TableName("sys_account")
public class SysAccount {

    /** 雪花算法主键 ID。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 统一用户中心用户 ID。 */
    private Long userId;
    /** 当前完全可支配的可用现金余额。 */
    private BigDecimal availableAmount;
    /** 提现中或订单待支付中被锁定的冻结现金。 */
    private BigDecimal frozenAmount;
    /** 已获得但处于在途、未到账的待结算现金。 */
    private BigDecimal settlingAmount;
    /** 历史累计获得的现金总额，只增不减。 */
    private BigDecimal totalAmount;
    /** 乐观锁版本号，用于 DB 级并发控制。 */
    private Integer version;
    /** 创建人用户 ID，0 代表系统自动初始化。 */
    private Long createBy;
    /** 账户开户时间。 */
    private LocalDateTime createTime;
    /** 最后修改人用户 ID。 */
    private Long updateBy;
    /** 大账最后更新时间。 */
    private LocalDateTime updateTime;

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

    public BigDecimal getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(BigDecimal availableAmount) {
        this.availableAmount = availableAmount;
    }

    public BigDecimal getFrozenAmount() {
        return frozenAmount;
    }

    public void setFrozenAmount(BigDecimal frozenAmount) {
        this.frozenAmount = frozenAmount;
    }

    public BigDecimal getSettlingAmount() {
        return settlingAmount;
    }

    public void setSettlingAmount(BigDecimal settlingAmount) {
        this.settlingAmount = settlingAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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
