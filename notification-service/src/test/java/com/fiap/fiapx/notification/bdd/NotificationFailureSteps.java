package com.fiap.fiapx.notification.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

public class NotificationFailureSteps {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.rabbitmq.events.exchange:video.processing.events.exchange}")
    private String eventsExchange;

    @Value("${spring.rabbitmq.events.routing-key.completed:video.processing.completed}")
    private String completedRoutingKey;

    @Given("the notification service is ready")
    public void givenNotificationServiceIsReady() {
        // Context is up with RabbitMQ and stub auth; no extra setup.
    }

    @When("a processing failure event is published for video {string} and user {string} with message {string}")
    public void whenProcessingFailureEventIsPublished(String videoId, String userId, String errorMessage) {
        Object payload = new FailureEventPayload(
                videoId,
                false,
                null,
                null,
                errorMessage != null ? errorMessage : "Erro desconhecido",
                userId
        );
        rabbitTemplate.convertAndSend(eventsExchange, completedRoutingKey, payload);
    }

    @Then("an email is sent to the user with the failure details for video {string}")
    public void thenEmailIsSentWithFailureDetails(String videoId) {
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(mailSender).send(argThat((SimpleMailMessage msg) ->
                        msg.getTo() != null
                                && msg.getTo().length > 0
                                && "bdd-user@example.com".equals(msg.getTo()[0])
                                && msg.getText() != null
                                && msg.getText().contains(videoId)
                ))
        );
    }

    private record FailureEventPayload(
            String videoId,
            boolean success,
            Integer frameCount,
            String zipPath,
            String errorMessage,
            String userId
    ) {}
}
