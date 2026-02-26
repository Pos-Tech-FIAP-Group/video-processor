package com.fiap.fiapx.processing.adapters.driven.infra.messaging;

import com.fiap.fiapx.processing.core.application.ports.ProcessingEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adapter para publicar eventos de processamento usando RabbitMQ.
 */
@Component
public class RabbitMqProcessingEventPublisher implements ProcessingEventPublisherPort {
    
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqProcessingEventPublisher.class);
    
    private final RabbitTemplate rabbitTemplate;
    private final String eventsExchange;
    private final String completedRoutingKey;
    private final String failedRoutingKey;
    
    public RabbitMqProcessingEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${spring.rabbitmq.events.exchange:video.processing.events.exchange}") String eventsExchange,
            @Value("${spring.rabbitmq.events.routing-key.completed:video.processing.completed}") String completedRoutingKey,
            @Value("${spring.rabbitmq.events.routing-key.failed:video.processing.failed}") String failedRoutingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.eventsExchange = eventsExchange;
        this.completedRoutingKey = completedRoutingKey;
        this.failedRoutingKey = failedRoutingKey;
    }
    
    @Override
    public void publishProcessingCompleted(String videoId, String resultLocation, double frameIntervalSeconds) {
        try {
            ProcessingCompletedEvent event = new ProcessingCompletedEvent(
                videoId,
                "COMPLETED",
                resultLocation,
                frameIntervalSeconds,
                java.time.LocalDateTime.now()
            );
            
            rabbitTemplate.convertAndSend(eventsExchange, completedRoutingKey, event);
            logger.info("Published processing completed event for videoId: {}", videoId);
        } catch (Exception e) {
            logger.error("Failed to publish processing completed event for videoId: {}", videoId, e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
    
    @Override
    public void publishProcessingFailed(String videoId, String errorMessage) {
        try {
            ProcessingFailedEvent event = new ProcessingFailedEvent(
                videoId,
                "FAILED",
                errorMessage,
                java.time.LocalDateTime.now()
            );
            
            rabbitTemplate.convertAndSend(eventsExchange, failedRoutingKey, event);
            logger.info("Published processing failed event for videoId: {}", videoId);
        } catch (Exception e) {
            logger.error("Failed to publish processing failed event for videoId: {}", videoId, e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
    
    /**
     * DTO interno para evento de processamento concluído.
     */
    public record ProcessingCompletedEvent(
        String videoId,
        String status,
        String resultLocation,
        double frameIntervalSeconds,
        java.time.LocalDateTime processedAt
    ) {}
    
    /**
     * DTO interno para evento de falha no processamento.
     */
    public record ProcessingFailedEvent(
        String videoId,
        String status,
        String errorMessage,
        java.time.LocalDateTime failedAt
    ) {}
}
