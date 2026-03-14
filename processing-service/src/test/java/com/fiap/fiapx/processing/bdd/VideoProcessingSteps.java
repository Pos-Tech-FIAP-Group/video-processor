package com.fiap.fiapx.processing.bdd;

import com.fiap.fiapx.processing.adapters.driver.api.dto.request.VideoProcessingMessage;
import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Steps BDD para o fluxo de consumo de mensagens de processamento e publicação de eventos.
 * Segue o mesmo padrão do auth-service (Cucumber + Spring Boot Test).
 */
public class VideoProcessingSteps {

    private static final String PROCESSING_ROUTING_KEY = "video.processing.requested";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.processing.exchange:video.processing.exchange}")
    private String processingExchange;

    @Value("${spring.rabbitmq.events.queue.completed:video.processing.completed.processing-service}")
    private String completedQueue;

    private String lastVideoId;
    private Map<String, Object> lastEventPayload;

    @Given("the processing queue is ready")
    public void givenProcessingQueueIsReady() {
        // A fila e o binding são criados pelo RabbitMqConfig ao subir o contexto.
        // O RabbitMQ container já está rodando via CucumberSpringConfig.
    }

    @When("I publish a video processing request with videoId {string} input path {string} and userId {string}")
    public void whenIPublishVideoProcessingRequest(String videoId, String inputPath, String userId) {
        lastVideoId = videoId;
        VideoProcessingMessage message = new VideoProcessingMessage(
                videoId,
                inputPath,
                1.0,
                userId,
                VideoFormat.MP4
        );
        rabbitTemplate.convertAndSend(processingExchange, PROCESSING_ROUTING_KEY, message);
    }

    @Then("within 15 seconds a failed event is published for videoId {string}")
    public void thenFailedEventIsPublished(String videoId) {
        lastVideoId = videoId;
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        await()
                .atMost(15, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Message message = rabbitTemplate.receive(completedQueue, 2_000);
                    assertThat(message).as("Expected a message on queue %s", completedQueue).isNotNull();
                    try {
                        lastEventPayload = mapper.readValue(
                                message.getBody(),
                                new TypeReference<Map<String, Object>>() {}
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    assertThat(lastEventPayload.get("videoId")).isEqualTo(videoId);
                    assertThat(lastEventPayload.get("success")).isEqualTo(Boolean.FALSE);
                });
    }

    @And("the failed event contains an error message")
    public void andFailedEventContainsErrorMessage() {
        assertThat(lastEventPayload).isNotNull();
        Object errorMessage = lastEventPayload.get("errorMessage");
        assertThat(errorMessage).isNotNull();
        assertThat(errorMessage.toString()).isNotBlank();
    }
}
