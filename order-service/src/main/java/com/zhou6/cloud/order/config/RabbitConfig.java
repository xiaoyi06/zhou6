package com.zhou6.cloud.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单服务 RabbitMQ 生产端配置。
 */
@Configuration
public class RabbitConfig {

    private static final String DEAD_LETTER_EXCHANGE = "zhou6.account.dlx";
    private static final String SETTLE_DEAD_LETTER_ROUTING_KEY = "zhou6.account.settle.dlq";
    private static final String UNFREEZE_DEAD_LETTER_ROUTING_KEY = "zhou6.account.unfreeze.dlq";

    @Bean
    /**
     * 使用 Jackson 将订单服务发送的 MQ 消息转换为 JSON。
     *
     * @return JSON 消息转换器
     */
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    /**
     * 声明订单到账户的直连交换机。
     *
     * @param exchange 交换机名称
     * @return 持久化直连交换机
     */
    public DirectExchange orderAccountExchange(
            @Value("${zhou6.order.mq.exchange:zhou6.order.account}") String exchange) {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    /**
     * 声明账户结算队列，参数必须与 account-service 保持一致。
     *
     * @param queueName 队列名称
     * @return 持久化结算队列
     */
    public Queue accountSettleQueue(
            @Value("${zhou6.order.mq.settle-routing-key:zhou6.account.settle}") String queueName) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(SETTLE_DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    /**
     * 声明账户解冻队列，参数必须与 account-service 保持一致。
     *
     * @param queueName 队列名称
     * @return 持久化解冻队列
     */
    public Queue accountUnfreezeQueue(
            @Value("${zhou6.order.mq.unfreeze-routing-key:zhou6.account.unfreeze}") String queueName) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(UNFREEZE_DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    /**
     * 绑定订单账户交换机到结算队列。
     *
     * @param accountSettleQueue 结算队列
     * @param orderAccountExchange 订单账户交换机
     * @param routingKey 结算路由键
     * @return 绑定关系
     */
    public Binding accountSettleBinding(Queue accountSettleQueue, DirectExchange orderAccountExchange,
            @Value("${zhou6.order.mq.settle-routing-key:zhou6.account.settle}") String routingKey) {
        return BindingBuilder.bind(accountSettleQueue).to(orderAccountExchange).with(routingKey);
    }

    @Bean
    /**
     * 绑定订单账户交换机到解冻队列。
     *
     * @param accountUnfreezeQueue 解冻队列
     * @param orderAccountExchange 订单账户交换机
     * @param routingKey 解冻路由键
     * @return 绑定关系
     */
    public Binding accountUnfreezeBinding(Queue accountUnfreezeQueue, DirectExchange orderAccountExchange,
            @Value("${zhou6.order.mq.unfreeze-routing-key:zhou6.account.unfreeze}") String routingKey) {
        return BindingBuilder.bind(accountUnfreezeQueue).to(orderAccountExchange).with(routingKey);
    }

    @Bean
    /**
     * 配置 RabbitTemplate，统一使用 JSON 消息并开启 mandatory。
     *
     * @param connectionFactory RabbitMQ 连接工厂
     * @param rabbitMessageConverter JSON 消息转换器
     * @return RabbitMQ 发送模板
     */
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter rabbitMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(rabbitMessageConverter);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.addBeforePublishPostProcessors(message -> {
            message.getMessageProperties().getHeaders().remove("__TypeId__");
            message.getMessageProperties().getHeaders().remove("__ContentTypeId__");
            message.getMessageProperties().getHeaders().remove("__KeyTypeId__");
            return message;
        });
        return rabbitTemplate;
    }
}
