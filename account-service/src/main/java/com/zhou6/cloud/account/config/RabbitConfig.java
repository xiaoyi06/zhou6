package com.zhou6.cloud.account.config;

import java.util.concurrent.Executors;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.TaskExecutorAdapter;

/**
 * 账户服务 RabbitMQ 配置，声明队列、JSON 消息转换器和虚拟线程消费容器。
 */
@Configuration
public class RabbitConfig {

    private static final String DEAD_LETTER_EXCHANGE = "zhou6.account.dlx";
    private static final String SETTLE_DEAD_LETTER_ROUTING_KEY = "zhou6.account.settle.dlq";
    private static final String UNFREEZE_DEAD_LETTER_ROUTING_KEY = "zhou6.account.unfreeze.dlq";

    /**
     * 使用 Jackson 将 RabbitMQ JSON 消息转换为账户消息对象。
     *
     * @return JSON 消息转换器
     */
    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 声明冻结金额结算队列。
     *
     * @param queueName 队列名称
     * @return 持久化队列
     */
    @Bean
    public Queue accountSettleQueue(@Value("${zhou6.account.mq.settle-queue:zhou6.account.settle}") String queueName) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(SETTLE_DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * 声明订单取消解冻队列。
     *
     * @param queueName 队列名称
     * @return 持久化队列
     */
    @Bean
    public Queue accountUnfreezeQueue(
            @Value("${zhou6.account.mq.unfreeze-queue:zhou6.account.unfreeze}") String queueName) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(UNFREEZE_DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * 声明账户服务死信交换机。
     *
     * @return 死信直连交换机
     */
    @Bean
    public DirectExchange accountDeadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    /**
     * 声明结算失败死信队列。
     *
     * @return 持久化死信队列
     */
    @Bean
    public Queue accountSettleDeadLetterQueue() {
        return QueueBuilder.durable(SETTLE_DEAD_LETTER_ROUTING_KEY).build();
    }

    /**
     * 声明解冻失败死信队列。
     *
     * @return 持久化死信队列
     */
    @Bean
    public Queue accountUnfreezeDeadLetterQueue() {
        return QueueBuilder.durable(UNFREEZE_DEAD_LETTER_ROUTING_KEY).build();
    }

    /**
     * 绑定结算死信队列。
     *
     * @return 绑定关系
     */
    @Bean
    public Binding accountSettleDeadLetterBinding() {
        return BindingBuilder.bind(accountSettleDeadLetterQueue())
                .to(accountDeadLetterExchange())
                .with(SETTLE_DEAD_LETTER_ROUTING_KEY);
    }

    /**
     * 绑定解冻死信队列。
     *
     * @return 绑定关系
     */
    @Bean
    public Binding accountUnfreezeDeadLetterBinding() {
        return BindingBuilder.bind(accountUnfreezeDeadLetterQueue())
                .to(accountDeadLetterExchange())
                .with(UNFREEZE_DEAD_LETTER_ROUTING_KEY);
    }

    /**
     * 配置 RabbitMQ 监听容器，使用虚拟线程承载并发消费任务。
     *
     * @param configurer Spring Boot 默认监听容器配置器
     * @param connectionFactory RabbitMQ 连接工厂
     * @param rabbitMessageConverter JSON 消息转换器
     * @return 监听容器工厂
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            MessageConverter rabbitMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);
        factory.setTaskExecutor(new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor()));
        return factory;
    }
}
