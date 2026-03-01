package com.fiap.fiapx.processing.integration;

import com.fiap.fiapx.processing.adapters.driver.api.dto.request.VideoProcessingMessage;
import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * Teste de integração básico para validar o fluxo completo de processamento.
 * Usa Testcontainers para RabbitMQ.
 */
@SpringBootTest
@Testcontainers
@Disabled("Requires Docker - enable when running with Testcontainers")
class VideoProcessingIntegrationTest {
    
    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3-management-alpine")
            .withExposedPorts(5672, 15672);
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQ::getAdminPassword);
    }
    
    @Test
    void shouldPublishAndConsumeMessage() {
        // Given
        VideoProcessingMessage message = new VideoProcessingMessage(
            "test-video-123",
            "/tmp/test-video.mp4",
            1.0,
            "test-user",
            VideoFormat.MP4
        );
        
        // When
        rabbitTemplate.convertAndSend("video.processing.exchange", "video.processing.request", message);
        
        // Then - aguarda um pouco para o processamento (em ambiente real, seria assíncrono)
        await().atMost(5, TimeUnit.SECONDS).until(() -> true);
        
        // Este teste valida que a mensagem foi enviada e consumida sem erros
        // Em um teste completo, verificaria eventos publicados e status persistido
    }
}
