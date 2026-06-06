package com.zhou6.cloud.workflow.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工作流事件 RabbitMQ 配置。
 */
@Configuration
public class RabbitConfig {

    /**
     * 声明工作流事件交换机。
     *
     * @param exchangeName 交换机名称
     * @return DirectExchange 实例
     */
    @Bean
    public DirectExchange workflowEventExchange(
            @Value("${zhou6.workflow.mq.event-exchange:zhou6.workflow.event.exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    /**
     * 使用 Jackson 将工作流事件消息转换为 JSON。
     *
     * @return JSON 消息转换器
     */
    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置 RabbitTemplate，统一使用 JSON 消息并开启 mandatory。
     *
     * @param connectionFactory RabbitMQ 连接工厂
     * @param rabbitMessageConverter JSON 消息转换器
     * @return RabbitMQ 发送模板
     */
    @Bean
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
