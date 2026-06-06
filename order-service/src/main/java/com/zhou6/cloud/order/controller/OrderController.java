package com.zhou6.cloud.order.controller;

import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.order.constant.OrderApiPathConstants;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.order.dto.OrderCreateDTO;
import com.zhou6.cloud.order.dto.OrderRefundDTO;
import com.zhou6.cloud.order.dto.OrderSnDTO;
import com.zhou6.cloud.order.handler.OrderErrorCode;
import com.zhou6.cloud.order.service.OrderService;
import com.zhou6.cloud.order.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "订单", description = "提供订单创建支付、详情查询和退款审批接口")
@RestController
@RequestMapping(OrderApiPathConstants.ORDER)
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建订单并立即完成现金预冻结支付。
     *
     * @param dto 订单金额参数
     * @return 支付后的订单详情
     */
    @PostMapping("/createPay")
    @Operation(summary = "创建并支付订单", description = "创建订单、同步预冻结现金、提交后发送账户结算消息")
    public R<OrderVO> createAndPay(@RequestBody OrderCreateDTO dto) {
        return R.ok(orderService.createAndPay(dto));
    }

    /**
     * 按订单号查询订单详情。
     *
     * @param dto 订单号参数
     * @return 订单详情
     */
    @PostMapping("/detail")
    @Operation(summary = "查询订单详情", description = "按订单号查询订单状态和金额")
    public R<OrderVO> detail(@RequestBody OrderSnDTO dto) {
        if (dto == null) {
            throw new BizException(OrderErrorCode.PARAM_INVALID);
        }
        return R.ok(orderService.detail(dto.getOrderSn()));
    }

    /**
     * 审批同意退款。
     *
     * @param dto 退款订单号参数
     * @return 空响应
     */
    @PostMapping("/refund/approve")
    @Operation(summary = "同意退款", description = "将已支付订单推进到退款完成，并同步调用账户红字冲正")
    public R<Void> approveRefund(@RequestBody OrderRefundDTO dto) {
        if (dto == null) {
            throw new BizException(OrderErrorCode.PARAM_INVALID);
        }
        orderService.approveRefund(dto.getOrderSn());
        return R.ok(null);
    }
}
