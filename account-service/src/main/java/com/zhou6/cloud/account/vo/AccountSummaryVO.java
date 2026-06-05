package com.zhou6.cloud.account.vo;

import java.math.BigDecimal;

/**
 * 首页现金看板响应对象。
 */
public class AccountSummaryVO {

    /** 当前完全可支配的可用现金余额。 */
    private BigDecimal available;
    /** 提现中或订单待支付中被锁定的冻结现金。 */
    private BigDecimal frozen;
    /** 已获得但处于在途、未到账的待结算现金。 */
    private BigDecimal settling;
    /** 历史累计获得的现金总额。 */
    private BigDecimal total;

    public BigDecimal getAvailable() {
        return available;
    }

    public void setAvailable(BigDecimal available) {
        this.available = available;
    }

    public BigDecimal getFrozen() {
        return frozen;
    }

    public void setFrozen(BigDecimal frozen) {
        this.frozen = frozen;
    }

    public BigDecimal getSettling() {
        return settling;
    }

    public void setSettling(BigDecimal settling) {
        this.settling = settling;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
