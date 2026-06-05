package com.zhou6.cloud.order.service;

import com.zhou6.cloud.order.dto.OrderCreateDTO;
import com.zhou6.cloud.order.vo.OrderVO;

/**
 * 订单业务服务。
 */
public interface OrderService {

    /**
     * 创建订单并立即发起现金支付。
     *
     * @param dto 订单金额参数
     * @return 创建并支付后的订单详情
     */
    OrderVO createAndPay(OrderCreateDTO dto);

    /**
     * 查询订单详情。
     *
     * @param orderSn 订单唯一业务单号
     * @return 订单详情
     */
    OrderVO detail(String orderSn);

    /**
     * 取消超时仍处于待支付状态的订单。
     *
     * @param orderSn 订单唯一业务单号
     */
    void cancelTimeoutOrder(String orderSn);

    /**
     * 审批同意退款，并同步调用账户服务做红字冲正。
     *
     * @param orderSn 订单唯一业务单号
     */
    void approveRefund(String orderSn);
}
