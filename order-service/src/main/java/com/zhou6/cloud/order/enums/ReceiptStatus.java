package com.zhou6.cloud.order.enums;

/**
 * 订单现金扣减凭证状态。
 */
public enum ReceiptStatus {

    /** 已预冻结，账户系统已经锁定现金。 */
    FROZEN(1),
    /** 已确认扣除，冻结金额已经异步结算落库。 */
    SETTLED(2),
    /** 已反向释放，冻结金额已经退回可用域。 */
    RELEASED(3);

    private final int code;

    ReceiptStatus(int code) {
        this.code = code;
    }

    /**
     * 获取数据库中保存的凭证状态码。
     *
     * @return 凭证状态码
     */
    public int getCode() {
        return code;
    }
}
