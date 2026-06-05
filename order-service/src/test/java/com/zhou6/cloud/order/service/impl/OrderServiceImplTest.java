package com.zhou6.cloud.order.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhou6.cloud.common.context.UserContextHolder;
import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.order.client.AccountClient;
import com.zhou6.cloud.order.dto.AccountAmountDTO;
import com.zhou6.cloud.order.dto.AccountReverseDTO;
import com.zhou6.cloud.order.dto.OrderCreateDTO;
import com.zhou6.cloud.order.entity.OmsOrder;
import com.zhou6.cloud.order.enums.OrderStatus;
import com.zhou6.cloud.order.enums.ReceiptStatus;
import com.zhou6.cloud.order.mapper.OmsOrderMapper;
import com.zhou6.cloud.order.mapper.OmsOrderPayReceiptMapper;
import com.zhou6.cloud.order.mq.AccountMessage;
import com.zhou6.cloud.order.mq.AccountMessagePublisher;
import com.zhou6.cloud.order.vo.OrderVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 订单业务服务单元测试，覆盖订单状态机和账户服务协作的关键分支。
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final Long USER_ID = 1001L;

    @Mock
    private OmsOrderMapper orderMapper;
    @Mock
    private OmsOrderPayReceiptMapper receiptMapper;
    @Mock
    private AccountClient accountClient;
    @Mock
    private AccountMessagePublisher accountMessagePublisher;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderMapper, receiptMapper, accountClient, accountMessagePublisher);
        UserContextHolder.setUserId(USER_ID);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    /**
     * 创建并支付成功时，应插入订单和凭证，推进订单状态，并注册提交/回滚两类账户消息。
     */
    @Test
    void createAndPayShouldFreezeAccountAndPublishAfterCommitMessages() {
        OrderCreateDTO dto = createRequest("20.0000", "12.5000");
        AtomicReference<OmsOrder> savedOrder = new AtomicReference<>();
        when(orderMapper.insert(any(OmsOrder.class))).thenAnswer(invocation -> {
            OmsOrder order = invocation.getArgument(0);
            savedOrder.set(order);
            return 1;
        });
        when(accountClient.freeze(any(AccountAmountDTO.class))).thenReturn(R.ok(Boolean.TRUE));
        when(receiptMapper.insert(any(com.zhou6.cloud.order.entity.OmsOrderPayReceipt.class))).thenReturn(1);
        when(orderMapper.updateStatus(anyString(), anyInt(), anyInt(), anyLong())).thenAnswer(invocation -> {
            savedOrder.get().setOrderStatus(invocation.getArgument(2));
            return 1;
        });
        when(orderMapper.selectOne(org.mockito.ArgumentMatchers.<LambdaQueryWrapper<OmsOrder>>any()))
                .thenAnswer(invocation -> savedOrder.get());

        OrderVO result = orderService.createAndPay(dto);

        assertEquals(USER_ID, result.getUserId());
        assertEquals(OrderStatus.PAID.getCode(), result.getOrderStatus());
        assertEquals(new BigDecimal("12.5000"), result.getPayAmount());

        ArgumentCaptor<AccountAmountDTO> freezeCaptor = ArgumentCaptor.forClass(AccountAmountDTO.class);
        verify(accountClient).freeze(freezeCaptor.capture());
        assertEquals(USER_ID, freezeCaptor.getValue().getUserId());
        assertEquals("ORDER_PAY", freezeCaptor.getValue().getBizType());
        assertEquals(savedOrder.get().getOrderSn(), freezeCaptor.getValue().getBizId());

        verify(orderMapper).updateStatus(savedOrder.get().getOrderSn(), OrderStatus.WAIT_PAY.getCode(),
                OrderStatus.PAID.getCode(), USER_ID);
        verify(accountMessagePublisher).sendUnfreezeAfterRollback(any(AccountMessage.class));
        verify(accountMessagePublisher).sendSettleAfterCommit(any(AccountMessage.class));
    }

    /**
     * 账户预冻结失败时，应抛出业务异常，并且不能插入凭证或发送 MQ。
     */
    @Test
    void createAndPayShouldStopWhenAccountFreezeFailed() {
        when(orderMapper.insert(any(OmsOrder.class))).thenReturn(1);
        when(accountClient.freeze(any(AccountAmountDTO.class))).thenReturn(R.fail("060002", "可用余额不足"));

        assertThrows(BizException.class, () -> orderService.createAndPay(createRequest("20.0000", "12.5000")));

        verify(receiptMapper, never()).insert(any(com.zhou6.cloud.order.entity.OmsOrderPayReceipt.class));
        verify(orderMapper, never()).updateStatus(anyString(), anyInt(), anyInt(), anyLong());
        verify(accountMessagePublisher, never()).sendUnfreezeAfterRollback(any(AccountMessage.class));
        verify(accountMessagePublisher, never()).sendSettleAfterCommit(any(AccountMessage.class));
    }

    /**
     * 超时取消待支付订单时，应按期望状态取消订单、释放凭证，并提交后发送账户解冻消息。
     */
    @Test
    void cancelTimeoutOrderShouldCancelWaitingOrderAndPublishUnfreezeMessage() {
        OmsOrder order = order("O202606060001", OrderStatus.WAIT_PAY.getCode(), new BigDecimal("8.0000"));
        when(orderMapper.selectOne(org.mockito.ArgumentMatchers.<LambdaQueryWrapper<OmsOrder>>any())).thenReturn(order);
        when(orderMapper.updateStatus(order.getOrderSn(), OrderStatus.WAIT_PAY.getCode(),
                OrderStatus.CANCELED.getCode(), 0L)).thenReturn(1);
        when(receiptMapper.updateStatus(order.getOrderSn(), ReceiptStatus.FROZEN.getCode(),
                ReceiptStatus.RELEASED.getCode(), 0L)).thenReturn(1);

        orderService.cancelTimeoutOrder(order.getOrderSn());

        verify(accountMessagePublisher).sendUnfreezeAfterCommit(any(AccountMessage.class));
    }

    /**
     * 退款审批成功时，应先进入退款中，账户冲正成功后再推进到已退款。
     */
    @Test
    void approveRefundShouldReverseAccountAndMarkRefunded() {
        OmsOrder order = order("O202606060002", OrderStatus.PAID.getCode(), new BigDecimal("9.0000"));
        when(orderMapper.selectOne(org.mockito.ArgumentMatchers.<LambdaQueryWrapper<OmsOrder>>any())).thenReturn(order);
        when(orderMapper.updateStatus(anyString(), anyInt(), anyInt(), eq(USER_ID))).thenReturn(1);
        when(accountClient.reverse(any(AccountReverseDTO.class))).thenReturn(R.ok(null));

        orderService.approveRefund(order.getOrderSn());

        verify(orderMapper).updateStatus(order.getOrderSn(), OrderStatus.PAID.getCode(),
                OrderStatus.REFUNDING.getCode(), USER_ID);
        verify(orderMapper).updateStatus(order.getOrderSn(), OrderStatus.REFUNDING.getCode(),
                OrderStatus.REFUNDED.getCode(), USER_ID);
        ArgumentCaptor<AccountReverseDTO> reverseCaptor = ArgumentCaptor.forClass(AccountReverseDTO.class);
        verify(accountClient).reverse(reverseCaptor.capture());
        assertEquals("ORDER_PAY", reverseCaptor.getValue().getOrigBizType());
        assertEquals("ORDER_REFUND", reverseCaptor.getValue().getReverseBizType());
        assertEquals(order.getOrderSn(), reverseCaptor.getValue().getOrigBizId());
    }

    /**
     * 实付金额大于订单总金额时，应在本地参数校验阶段失败，不写数据库。
     */
    @Test
    void createAndPayShouldRejectPayAmountGreaterThanTotalAmount() {
        assertThrows(BizException.class, () -> orderService.createAndPay(createRequest("10.0000", "12.0000")));

        verify(orderMapper, never()).insert(any(OmsOrder.class));
        verify(accountClient, never()).freeze(any(AccountAmountDTO.class));
    }

    private OrderCreateDTO createRequest(String totalAmount, String payAmount) {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setTotalAmount(new BigDecimal(totalAmount));
        dto.setPayAmount(new BigDecimal(payAmount));
        return dto;
    }

    private OmsOrder order(String orderSn, int status, BigDecimal payAmount) {
        OmsOrder order = new OmsOrder();
        order.setOrderSn(orderSn);
        order.setUserId(USER_ID);
        order.setTotalAmount(payAmount);
        order.setPayAmount(payAmount);
        order.setOrderStatus(status);
        order.setCreateBy(USER_ID);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateBy(USER_ID);
        order.setUpdateTime(LocalDateTime.now());
        return order;
    }
}
