package com.zhou6.cloud.order.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 账户消息生产器，保证只有本地事务提交成功后才投递 MQ。
 */
@Component
public class AccountMessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String settleRoutingKey;
    private final String unfreezeRoutingKey;

    public AccountMessagePublisher(RabbitTemplate rabbitTemplate,
            @Value("${zhou6.order.mq.exchange:zhou6.order.account}") String exchange,
            @Value("${zhou6.order.mq.settle-routing-key:zhou6.account.settle}") String settleRoutingKey,
            @Value("${zhou6.order.mq.unfreeze-routing-key:zhou6.account.unfreeze}") String unfreezeRoutingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.settleRoutingKey = settleRoutingKey;
        this.unfreezeRoutingKey = unfreezeRoutingKey;
    }

    /**
     * 本地订单事务提交成功后，发送账户冻结金额结算消息。
     *
     * @param message 账户结算消息
     */
    public void sendSettleAfterCommit(AccountMessage message) {
        sendAfterCommit(settleRoutingKey, message);
    }

    /**
     * 本地订单事务提交成功后，发送账户解冻消息。
     *
     * @param message 账户解冻消息
     */
    public void sendUnfreezeAfterCommit(AccountMessage message) {
        sendAfterCommit(unfreezeRoutingKey, message);
    }

    /**
     * 本地订单事务回滚后，发送账户解冻补偿消息。
     *
     * @param message 账户解冻消息
     */
    public void sendUnfreezeAfterRollback(AccountMessage message) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            rabbitTemplate.convertAndSend(exchange, unfreezeRoutingKey, message);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    rabbitTemplate.convertAndSend(exchange, unfreezeRoutingKey, message);
                }
            }
        });
    }

    /**
     * 注册事务提交后的 MQ 发送动作；无事务上下文时立即发送。
     *
     * @param routingKey RabbitMQ 路由键
     * @param message 账户消息
     */
    private void sendAfterCommit(String routingKey, AccountMessage message) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                rabbitTemplate.convertAndSend(exchange, routingKey, message);
            }
        });
    }
}
