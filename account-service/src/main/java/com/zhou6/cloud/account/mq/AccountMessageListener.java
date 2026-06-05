package com.zhou6.cloud.account.mq;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.zhou6.cloud.account.handler.AccountErrorCode;
import com.zhou6.cloud.account.service.AccountService;
import com.zhou6.cloud.common.handler.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 账户 MQ 消费者，负责订单结算和订单取消解冻后的本地事务落库。
 */
@Component
public class AccountMessageListener {

    private static final Logger log = LoggerFactory.getLogger(AccountMessageListener.class);

    private final AccountService accountService;
    private final int maxRedeliveryCount;

    public AccountMessageListener(AccountService accountService,
            @Value("${zhou6.account.mq.max-redelivery-count:3}") int maxRedeliveryCount) {
        this.accountService = accountService;
        this.maxRedeliveryCount = maxRedeliveryCount;
    }

    /**
     * 消费冻结金额结算消息，插入流水并扣减冻结金额。
     *
     * @param message 账户结算消息
     * @param rawMessage RabbitMQ 原始消息
     * @param channel RabbitMQ 通道，用于手动 ACK/NACK
     * @throws IOException ACK/NACK 失败时抛出
     */
    @RabbitListener(queues = "${zhou6.account.mq.settle-queue:zhou6.account.settle}",
            autoStartup = "${zhou6.account.mq.listener-auto-startup:false}", ackMode = "MANUAL")
    public void onSettle(AccountMessage message, Message rawMessage, Channel channel) throws IOException {
        try {
            accountService.settleFrozen(message.getUserId(), message.getAmount(), message.getBizType(),
                    message.getBizId(), message.getOperatorId(), message.getRemark());
            ack(rawMessage, channel);
        } catch (BizException ex) {
            handleBizException(message, rawMessage, channel, ex);
        } catch (Exception ex) {
            nack(rawMessage, channel, ex);
        }
    }

    /**
     * 消费订单取消解冻消息，插入流水并将冻结金额释放回可用金额。
     *
     * @param message 账户解冻消息
     * @param rawMessage RabbitMQ 原始消息
     * @param channel RabbitMQ 通道，用于手动 ACK/NACK
     * @throws IOException ACK/NACK 失败时抛出
     */
    @RabbitListener(queues = "${zhou6.account.mq.unfreeze-queue:zhou6.account.unfreeze}",
            autoStartup = "${zhou6.account.mq.listener-auto-startup:false}", ackMode = "MANUAL")
    public void onUnfreeze(AccountMessage message, Message rawMessage, Channel channel) throws IOException {
        try {
            accountService.unfreeze(message.getUserId(), message.getAmount(), message.getBizType(), message.getBizId(),
                    message.getOperatorId(), message.getRemark());
            ack(rawMessage, channel);
        } catch (BizException ex) {
            handleBizException(message, rawMessage, channel, ex);
        } catch (Exception ex) {
            nack(rawMessage, channel, ex);
        }
    }

    private void handleBizException(AccountMessage message, Message rawMessage, Channel channel, BizException ex)
            throws IOException {
        if (AccountErrorCode.BIZ_ID_DUPLICATED.getCode().equals(ex.getCode())) {
            log.info("Duplicate account message acknowledged: bizType={}, bizId={}",
                    message.getBizType(), message.getBizId());
            ack(rawMessage, channel);
            return;
        }
        nack(rawMessage, channel, ex);
    }

    private void ack(Message rawMessage, Channel channel) throws IOException {
        channel.basicAck(rawMessage.getMessageProperties().getDeliveryTag(), false);
    }

    private void nack(Message rawMessage, Channel channel, Exception ex) throws IOException {
        long retryCount = retryCount(rawMessage);
        boolean requeue = retryCount < maxRedeliveryCount;
        if (requeue) {
            log.warn("Account message failed, requeue message: retryCount={}, maxRedeliveryCount={}",
                    retryCount, maxRedeliveryCount, ex);
            channel.basicNack(rawMessage.getMessageProperties().getDeliveryTag(), false, true);
            return;
        }
        log.error("Account message failed and routed to dead-letter queue: retryCount={}, maxRedeliveryCount={}",
                retryCount, maxRedeliveryCount, ex);
        channel.basicNack(rawMessage.getMessageProperties().getDeliveryTag(), false, false);
    }

    private long retryCount(Message rawMessage) {
        Object value = rawMessage.getMessageProperties().getHeaders().get("x-zhou6-retry-count");
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ex) {
                return maxRedeliveryCount;
            }
        }
        return rawMessage.getMessageProperties().isRedelivered() ? maxRedeliveryCount : 0L;
    }
}
