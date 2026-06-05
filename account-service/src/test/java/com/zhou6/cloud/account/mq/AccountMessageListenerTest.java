package com.zhou6.cloud.account.mq;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import com.rabbitmq.client.Channel;
import com.zhou6.cloud.account.handler.AccountErrorCode;
import com.zhou6.cloud.account.service.AccountService;
import com.zhou6.cloud.common.handler.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

class AccountMessageListenerTest {

    private AccountService accountService;
    private AccountMessageListener listener;
    private Channel channel;

    @BeforeEach
    void setUp() {
        accountService = mock(AccountService.class);
        listener = new AccountMessageListener(accountService, 1);
        channel = mock(Channel.class);
    }

    @Test
    void duplicateSettleMessageIsAcknowledged() throws Exception {
        AccountMessage message = message();
        Message rawMessage = rawMessage(false);
        Mockito.doThrow(new BizException(AccountErrorCode.BIZ_ID_DUPLICATED))
                .when(accountService)
                .settleFrozen(any(), any(), any(), any(), any(), any());

        listener.onSettle(message, rawMessage, channel);

        verify(channel).basicAck(99L, false);
        verify(channel, never()).basicNack(any(Long.class), any(Boolean.class), any(Boolean.class));
    }

    @Test
    void firstGenericFailureIsRequeued() throws Exception {
        AccountMessage message = message();
        Message rawMessage = rawMessage(false);
        Mockito.doThrow(new IllegalStateException("temporary failure"))
                .when(accountService)
                .unfreeze(any(), any(), any(), any(), any(), any());

        listener.onUnfreeze(message, rawMessage, channel);

        verify(channel).basicNack(99L, false, true);
        verify(channel, never()).basicAck(any(Long.class), any(Boolean.class));
    }

    @Test
    void redeliveredGenericFailureIsRejectedToDeadLetterQueue() throws Exception {
        AccountMessage message = message();
        Message rawMessage = rawMessage(true);
        Mockito.doThrow(new IllegalStateException("permanent failure"))
                .when(accountService)
                .unfreeze(any(), any(), any(), any(), any(), any());

        listener.onUnfreeze(message, rawMessage, channel);

        verify(channel).basicNack(99L, false, false);
        verify(channel, never()).basicAck(any(Long.class), any(Boolean.class));
    }

    private AccountMessage message() {
        AccountMessage message = new AccountMessage();
        message.setUserId(10001L);
        message.setAmount(new BigDecimal("1.0000"));
        message.setBizType("ORDER_PAY");
        message.setBizId("order-1");
        message.setOperatorId(10001L);
        message.setRemark("test");
        return message;
    }

    private Message rawMessage(boolean redelivered) {
        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(99L);
        properties.setRedelivered(redelivered);
        return new Message(new byte[0], properties);
    }
}
