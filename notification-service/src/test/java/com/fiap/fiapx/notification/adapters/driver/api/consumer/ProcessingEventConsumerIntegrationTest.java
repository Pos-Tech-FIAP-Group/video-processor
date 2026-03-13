package com.fiap.fiapx.notification.adapters.driver.api.consumer;

import com.fiap.fiapx.notification.NotificationServiceApplication;
import com.fiap.fiapx.notification.core.application.ports.GetUserByUuidPort;
import com.fiap.fiapx.notification.core.domain.model.UserInfo;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = NotificationServiceApplication.class)
@ActiveProfiles("integration")
@Testcontainers
@Import(ProcessingEventConsumerIntegrationTest.TestConfig.class)
class ProcessingEventConsumerIntegrationTest {

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.events.exchange:video.processing.events.exchange}")
    private String eventsExchange;

    @Value("${spring.rabbitmq.events.routing-key.completed:video.processing.completed}")
    private String completedRoutingKey;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void ao_receber_evento_de_falha_com_userId_chama_auth_e_envia_email_para_o_destinatario() {
        String userUuid = "550e8400-e29b-41d4-a716-446655440000";
        String videoId = "video-integration-1";
        String errorMessage = "Falha simulada no processamento";

        Object payload = new FailureEventPayload(
                videoId,
                false,
                null,
                null,
                errorMessage,
                userUuid
        );

        rabbitTemplate.convertAndSend(eventsExchange, completedRoutingKey, payload);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(mailSender).send(argThat((SimpleMailMessage msg) ->
                        msg.getTo() != null
                                && msg.getTo().length > 0
                                && "test-user@example.com".equals(msg.getTo()[0])
                                && msg.getText() != null
                                && msg.getText().contains(videoId)
                                && msg.getText().contains(errorMessage)
                ))
        );
    }

    /**
     * Payload no formato do evento (compativel com VideoProcessingEventRequest).
     */
    private record FailureEventPayload(
            String videoId,
            boolean success,
            Integer frameCount,
            String zipPath,
            String errorMessage,
            String userId
    ) {}

    @Configuration
    static class TestConfig {
        @Bean
        @Primary
        GetUserByUuidPort stubGetUserByUuidPort() {
            return userUuid -> Optional.of(new UserInfo("test-user@example.com", "TestUser"));
        }
    }
}
