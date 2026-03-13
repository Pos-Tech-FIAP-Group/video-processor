package com.fiap.fiapx.processing.adapters.driven.infra.messaging;

import com.fiap.fiapx.processing.core.domain.exception.EventPublishingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMqProcessingEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitMqProcessingEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new RabbitMqProcessingEventPublisher(
                rabbitTemplate,
                "video.processing.events.exchange",
                "video.processing.completed",
                "video.processing.failed"
        );
    }

    @Test
    void shouldPublishCompletedEventWithExpectedPayload() {
        String videoId = "video-123";
        String resultLocation = "https://bucket/video-123.zip";
        long frameCount = 42L;

        ArgumentCaptor<RabbitMqProcessingEventPublisher.VideoProcessingCompletedPayload> payloadCaptor =
                ArgumentCaptor.forClass(RabbitMqProcessingEventPublisher.VideoProcessingCompletedPayload.class);

        publisher.publishProcessingCompleted(videoId, resultLocation, frameCount);

        verify(rabbitTemplate).convertAndSend(
                eq("video.processing.events.exchange"),
                eq("video.processing.completed"),
                payloadCaptor.capture()
        );

        RabbitMqProcessingEventPublisher.VideoProcessingCompletedPayload payload = payloadCaptor.getValue();
        assertNotNull(payload);
        assertEquals(videoId, payload.videoId());
        assertEquals(true, payload.success());
        assertEquals((Integer) (int) frameCount, payload.frameCount());
        assertEquals(resultLocation, payload.zipPath());
        assertEquals(null, payload.errorMessage());
        assertEquals(null, payload.userId());
    }

    @Test
    void shouldPublishFailedEventWithErrorMessage() {
        String videoId = "video-456";
        String userId = "user-789";
        String errorMessage = "Something went wrong";

        ArgumentCaptor<RabbitMqProcessingEventPublisher.VideoProcessingCompletedPayload> payloadCaptor =
                ArgumentCaptor.forClass(RabbitMqProcessingEventPublisher.VideoProcessingCompletedPayload.class);

        publisher.publishProcessingFailed(videoId, userId, errorMessage);

        verify(rabbitTemplate).convertAndSend(
                eq("video.processing.events.exchange"),
                eq("video.processing.completed"),
                payloadCaptor.capture()
        );

        RabbitMqProcessingEventPublisher.VideoProcessingCompletedPayload payload = payloadCaptor.getValue();
        assertNotNull(payload);
        assertEquals(videoId, payload.videoId());
        assertEquals(false, payload.success());
        assertEquals(null, payload.frameCount());
        assertEquals(null, payload.zipPath());
        assertEquals(errorMessage, payload.errorMessage());
        assertEquals(userId, payload.userId());
    }

    @Test
    void shouldPublishFailedEventWithDefaultErrorMessageWhenNull() {
        String videoId = "video-789";
        String userId = "user-abc";

        ArgumentCaptor<RabbitMqProcessingEventPublisher.VideoProcessingCompletedPayload> payloadCaptor =
                ArgumentCaptor.forClass(RabbitMqProcessingEventPublisher.VideoProcessingCompletedPayload.class);

        publisher.publishProcessingFailed(videoId, userId, null);

        verify(rabbitTemplate).convertAndSend(
                eq("video.processing.events.exchange"),
                eq("video.processing.completed"),
                payloadCaptor.capture()
        );

        RabbitMqProcessingEventPublisher.VideoProcessingCompletedPayload payload = payloadCaptor.getValue();
        assertNotNull(payload);
        assertEquals(videoId, payload.videoId());
        assertEquals(false, payload.success());
        assertEquals("Erro desconhecido", payload.errorMessage());
        assertEquals(userId, payload.userId());
    }

    @Test
    void shouldWrapExceptionWhenPublishCompletedFails() {
        doThrow(new RuntimeException("RabbitMQ unavailable"))
                .when(rabbitTemplate)
                .convertAndSend(
                        eq("video.processing.events.exchange"),
                        eq("video.processing.completed"),
                        any(Object.class)
                );

        EventPublishingException ex = assertThrows(EventPublishingException.class, () ->
                publisher.publishProcessingCompleted("video-123", "location", 10L)
        );

        assertEquals("Failed to publish event", ex.getMessage());
    }

    @Test
    void shouldWrapExceptionWhenPublishFailedFails() {
        doThrow(new RuntimeException("RabbitMQ unavailable"))
                .when(rabbitTemplate)
                .convertAndSend(
                        eq("video.processing.events.exchange"),
                        eq("video.processing.completed"),
                        any(Object.class)
                );

        EventPublishingException ex = assertThrows(EventPublishingException.class, () ->
                publisher.publishProcessingFailed("video-123", "user-1", "error")
        );

        assertEquals("Failed to publish event", ex.getMessage());
    }
}

