package com.fiap.fiapx.notification.adapters.driver.api.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${spring.rabbitmq.events.exchange:video.processing.events.exchange}")
    private String eventsExchange;

    @Value("${spring.rabbitmq.events.routing-key.completed:video.processing.completed}")
    private String completedRoutingKey;

    @Value("${spring.rabbitmq.events.routing-key.failed:video.processing.failed}")
    private String failedRoutingKey;

    @Value("${spring.rabbitmq.notification.queue:video.processing.notification-service.queue}")
    private String notificationQueue;

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(eventsExchange, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(notificationQueue).build();
    }

    @Bean
    public Binding completedBinding() {
        return BindingBuilder
            .bind(notificationQueue())
            .to(eventsExchange())
            .with(completedRoutingKey);
    }

    @Bean
    public Binding failedBinding() {
        return BindingBuilder
            .bind(notificationQueue())
            .to(eventsExchange())
            .with(failedRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
        ConnectionFactory connectionFactory
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}