package com.zhou6.cloud.community.config;

import java.util.Map;
import java.util.concurrent.Executors;

import com.zhou6.cloud.community.mq.CommunityInteractionMessage;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.TaskExecutorAdapter;

@Configuration
public class RabbitConfig {

    private static final String DEAD_LETTER_EXCHANGE = "zhou6.community.dlx";
    private static final String INTERACTION_DEAD_LETTER_ROUTING_KEY = "zhou6.community.interaction.dlq";

    @Bean
    public MessageConverter rabbitMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setDefaultType(CommunityInteractionMessage.class);
        classMapper.setIdClassMapping(Map.of(
                "com.zhou6.cloud.community.mq.CommunityInteractionMessage", CommunityInteractionMessage.class));
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public DirectExchange communityInteractionExchange(
            @Value("${zhou6.community.mq.exchange:zhou6.community.interaction}") String exchange) {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    public Queue communityInteractionQueue(
            @Value("${zhou6.community.mq.interaction-queue:zhou6.community.interaction}") String queueName) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(INTERACTION_DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding communityInteractionBinding(Queue communityInteractionQueue,
            DirectExchange communityInteractionExchange,
            @Value("${zhou6.community.mq.interaction-routing-key:zhou6.community.interaction}") String routingKey) {
        return BindingBuilder.bind(communityInteractionQueue).to(communityInteractionExchange).with(routingKey);
    }

    @Bean
    public DirectExchange communityDeadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue communityInteractionDeadLetterQueue() {
        return QueueBuilder.durable(INTERACTION_DEAD_LETTER_ROUTING_KEY).build();
    }

    @Bean
    public Binding communityInteractionDeadLetterBinding() {
        return BindingBuilder.bind(communityInteractionDeadLetterQueue())
                .to(communityDeadLetterExchange())
                .with(INTERACTION_DEAD_LETTER_ROUTING_KEY);
    }

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
