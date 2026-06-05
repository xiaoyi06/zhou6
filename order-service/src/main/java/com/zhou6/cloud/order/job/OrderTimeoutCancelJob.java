package com.zhou6.cloud.order.job;

import java.time.LocalDateTime;
import java.util.List;

import com.zhou6.cloud.order.entity.OmsOrder;
import com.zhou6.cloud.order.enums.OrderStatus;
import com.zhou6.cloud.order.mapper.OmsOrderMapper;
import com.zhou6.cloud.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单超时取消任务，兜底释放异常卡在待支付状态的预冻结金额。
 */
@Component
public class OrderTimeoutCancelJob {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutCancelJob.class);

    private final OmsOrderMapper orderMapper;
    private final OrderService orderService;
    private final int timeoutMinutes;
    private final int batchSize;

    public OrderTimeoutCancelJob(OmsOrderMapper orderMapper, OrderService orderService,
            @Value("${zhou6.order.timeout-minutes:15}") int timeoutMinutes,
            @Value("${zhou6.order.timeout-batch-size:100}") int batchSize) {
        this.orderMapper = orderMapper;
        this.orderService = orderService;
        this.timeoutMinutes = timeoutMinutes;
        this.batchSize = batchSize;
    }

    /**
     * 扫描超时待支付订单并逐笔取消。
     */
    @Scheduled(fixedDelayString = "${zhou6.order.timeout-scan-delay-ms:60000}")
    public void cancelTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(timeoutMinutes);
        List<OmsOrder> orders = orderMapper.selectTimeoutOrders(OrderStatus.WAIT_PAY.getCode(), deadline, batchSize);
        for (OmsOrder order : orders) {
            try {
                orderService.cancelTimeoutOrder(order.getOrderSn());
            } catch (Exception ex) {
                log.warn("Cancel timeout order failed: orderSn={}", order.getOrderSn(), ex);
            }
        }
    }
}
