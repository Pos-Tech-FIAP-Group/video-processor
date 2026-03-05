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
    public void publishProcessingCompleted(String videoId, String resultLocation, long frameCount) {
        try {
            // Formato esperado pelo video-service: VideoProcessingCompletedMessage(videoId, success, frameCount, zipPath, errorMessage)
            var payload = new VideoProcessingCompletedPayload(
                videoId,
                true,
                (int) frameCount,
                resultLocation,
                null
            );
            rabbitTemplate.convertAndSend(eventsExchange, completedRoutingKey, payload);
            logger.info("Published processing completed event for videoId: {}, frameCount: {}", videoId, frameCount);
        } catch (Exception e) {
            logger.error("Failed to publish processing completed event for videoId: {}", videoId, e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    @Override
    public void publishProcessingFailed(String videoId, String errorMessage) {
        try {
            // Mesmo formato do completed para o video-service consumir com executeFailure
            var payload = new VideoProcessingCompletedPayload(
                videoId,
                false,
                null,
                null,
                errorMessage != null ? errorMessage : "Erro desconhecido"
            );
            // Envia na mesma routing key do completed para o video-service consumir na mesma fila e chamar executeFailure
            rabbitTemplate.convertAndSend(eventsExchange, completedRoutingKey, payload);
            logger.info("Published processing failed event for videoId: {}", videoId);
        } catch (Exception e) {
            logger.error("Failed to publish processing failed event for videoId: {}", videoId, e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    /**
     * Payload compatível com VideoProcessingCompletedMessage do video-service (videoId, success, frameCount, zipPath, errorMessage).
     */
    public record VideoProcessingCompletedPayload(
        String videoId,
        boolean success,
        Integer frameCount,
        String zipPath,
        String errorMessage
    ) {}
}
