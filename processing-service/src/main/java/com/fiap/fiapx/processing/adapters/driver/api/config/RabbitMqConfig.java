package com.fiap.fiapx.processing.adapters.driver.api.config;

import com.fiap.fiapx.processing.adapters.driver.api.exceptionhandler.ProcessingExceptionHandler;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do RabbitMQ para o processing-service.
 * Define exchanges, queues, bindings e configuração de paralelismo.
 */
@Configuration
public class RabbitMqConfig {
    
    // Exchange e Queue de entrada (requisições de processamento)
    @Value("${spring.rabbitmq.processing.exchange:video.processing.exchange}")
    private String processingExchange;
    
    @Value("${spring.rabbitmq.processing.queue:video.processing.queue}")
    private String processingQueue;
    
    @Value("${spring.rabbitmq.processing.routing-key:video.processing.requested}")
    private String processingRoutingKey;
    
    // Exchange e Queues de eventos (resultados)
    @Value("${spring.rabbitmq.events.exchange:video.processing.events.exchange}")
    private String eventsExchange;
    
    @Value("${spring.rabbitmq.events.queue.completed:video.processing.completed.processing-service}")
    private String completedQueue;
    
    @Value("${spring.rabbitmq.events.queue.failed:video.processing.failed.processing-service}")
    private String failedQueue;
    
    @Value("${spring.rabbitmq.events.routing-key.completed:video.processing.completed}")
    private String completedRoutingKey;
    
    @Value("${spring.rabbitmq.events.routing-key.failed:video.processing.failed}")
    private String failedRoutingKey;
    
    // Dead Letter Queue
    @Value("${spring.rabbitmq.dlq.exchange:video.processing.dlq.exchange}")
    private String dlqExchange;
    
    @Value("${spring.rabbitmq.dlq.queue:video.processing.dlq}")
    private String dlqQueue;
    
    // Configuração de paralelismo
    @Value("${spring.rabbitmq.listener.concurrency.min:5}")
    private int minConcurrentConsumers;
    
    @Value("${spring.rabbitmq.listener.concurrency.max:20}")
    private int maxConcurrentConsumers;
    
    @Value("${spring.rabbitmq.listener.prefetch:10}")
    private int prefetchCount;
    
    // Exchange de entrada (Topic)
    @Bean
    public TopicExchange processingExchange() {
        return new TopicExchange(processingExchange, true, false);
    }
    
    // Queue de entrada com DLQ
    @Bean
    public Queue processingQueue() {
        return QueueBuilder.durable(processingQueue)
                .withArgument("x-dead-letter-exchange", dlqExchange)
                .withArgument("x-dead-letter-routing-key", dlqQueue)
                .build();
    }
    
    // Binding de entrada
    @Bean
    public Binding processingBinding() {
        return BindingBuilder
                .bind(processingQueue())
                .to(processingExchange())
                .with(processingRoutingKey);
    }
    
    // Exchange de eventos (Topic)
    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(eventsExchange, true, false);
    }
    
    // Queue de eventos completados
    @Bean
    public Queue completedQueue() {
        return QueueBuilder.durable(completedQueue).build();
    }
    
    // Queue de eventos de falha
    @Bean
    public Queue failedQueue() {
        return QueueBuilder.durable(failedQueue).build();
    }
    
    // Bindings de eventos
    @Bean
    public Binding completedBinding() {
        return BindingBuilder
                .bind(completedQueue())
                .to(eventsExchange())
                .with(completedRoutingKey);
    }
    
    @Bean
    public Binding failedBinding() {
        return BindingBuilder
                .bind(failedQueue())
                .to(eventsExchange())
                .with(failedRoutingKey);
    }
    
    // Dead Letter Exchange e Queue
    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(dlqExchange, true, false);
    }
    
    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(dlqQueue).build();
    }
    
    @Bean
    public Binding dlqBinding() {
        return BindingBuilder
                .bind(dlqQueue())
                .to(dlqExchange())
                .with(dlqQueue);
    }
    
    // Message Converter (JSON)
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    // RabbitTemplate com JSON converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
    
    // Container Factory com paralelismo e ErrorHandler configurados
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            ProcessingExceptionHandler processingExceptionHandler) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setErrorHandler(processingExceptionHandler);

        // Configuração de paralelismo
        factory.setConcurrentConsumers(minConcurrentConsumers);
        factory.setMaxConcurrentConsumers(maxConcurrentConsumers);
        factory.setPrefetchCount(prefetchCount);

        // Rejeitadas não voltam para a fila (vão para DLQ)
        factory.setDefaultRequeueRejected(false);

        return factory;
    }
}
