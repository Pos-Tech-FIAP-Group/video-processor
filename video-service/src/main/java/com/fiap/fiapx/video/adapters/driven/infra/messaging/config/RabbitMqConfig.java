package com.fiap.fiapx.video.adapters.driven.infra.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "video.processing.exchange";

    public static final String RK_REQUESTED = "video.processing.requested";
    public static final String RK_COMPLETED = "video.processing.completed";

    public static final String QUEUE_COMPLETED_VIDEO_SERVICE = "video.processing.completed.video-service";

    @Bean
    public TopicExchange videoProcessingExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue completedQueue() {
        return QueueBuilder.durable(QUEUE_COMPLETED_VIDEO_SERVICE).build();
    }

    @Bean
    public Binding completedBinding(Queue completedQueue, TopicExchange videoProcessingExchange) {
        return BindingBuilder.bind(completedQueue).to(videoProcessingExchange).with(RK_COMPLETED);
    }

    @Bean
    public Jackson2JsonMessageConverter jacksonConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

}