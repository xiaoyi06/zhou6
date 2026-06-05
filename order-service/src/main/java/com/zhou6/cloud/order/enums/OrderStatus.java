package com.zhou6.cloud.order.enums;

/**
 * 订单状态机。
 */
public enum OrderStatus {

    /** 待支付，订单已创建但尚未确认支付完成。 */
    WAIT_PAY(10),
    /** 已支付，账户现金已预冻结，等待异步结算落库。 */
    PAID(20),
    /** 已取消，订单超时或用户取消后不再继续支付。 */
    CANCELED(30),
    /** 退款中，退款审批已通过但账户冲正尚未完全闭环。 */
    REFUNDING(40),
    /** 已退款，账户红字冲正成功。 */
    REFUNDED(50);

    private final int code;

    OrderStatus(int code) {
        this.code = code;
    }

    /**
     * 获取数据库中保存的订单状态码。
     *
     * @return 订单状态码
     */
    public int getCode() {
        return code;
    }
}
