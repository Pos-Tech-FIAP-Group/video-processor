package com.fiap.fiapx.processing.adapters.driver.api.consumer;

import com.fiap.fiapx.processing.adapters.driver.api.dto.request.VideoProcessingMessage;
import com.fiap.fiapx.processing.core.application.usecases.ProcessVideoUseCase;
import com.fiap.fiapx.processing.core.domain.model.VideoProcessingRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Consumer RabbitMQ para processar mensagens de requisição de processamento de vídeo.
 * Configurado com paralelismo para processar múltiplas requisições simultaneamente.
 */
@Component
@Validated
public class VideoProcessingConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(VideoProcessingConsumer.class);
    
    private final ProcessVideoUseCase processVideoUseCase;
    
    public VideoProcessingConsumer(ProcessVideoUseCase processVideoUseCase) {
        this.processVideoUseCase = processVideoUseCase;
    }
    
    /**
     * Listener para mensagens de processamento de vídeo.
     * Configurado com paralelismo via RabbitMqConfig.
     */
    @RabbitListener(
        queues = "${spring.rabbitmq.processing.queue:video.processing.queue}",
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void processVideoMessage(
            @Payload @Valid VideoProcessingMessage message,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        
        String videoId = message.videoId();
        MDC.put("videoId", videoId);
        
        try {
            logger.info("Received video processing message for videoId: {}, frameInterval: {}", 
                videoId, message.effectiveFrameIntervalSeconds());
            
            // Converte DTO da mensagem para modelo de domínio
            VideoProcessingRequest request = toVideoProcessingRequest(message);
            
            // Executa o use case
            processVideoUseCase.execute(request);
            
            logger.info("Successfully processed video message for videoId: {}", videoId);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid video processing request for videoId: {}", videoId, e);
            // Não faz requeue para erros de validação (erros permanentes)
            throw e;
        } catch (Exception e) {
            logger.error("Error processing video message for videoId: {}", videoId, e);
            // Re-throw para que o RabbitMQ possa fazer requeue ou enviar para DLQ
            throw new RuntimeException("Failed to process video: " + videoId, e);
        } finally {
            MDC.remove("videoId");
        }
    }
    
    private VideoProcessingRequest toVideoProcessingRequest(VideoProcessingMessage message) {
        Path inputPath = Paths.get(message.inputLocation());
        
        return new VideoProcessingRequest(
            message.videoId(),
            inputPath,
            message.effectiveFrameIntervalSeconds(),
            message.format(),
            message.userId() != null ? message.userId() : "system"
        );
    }
}
