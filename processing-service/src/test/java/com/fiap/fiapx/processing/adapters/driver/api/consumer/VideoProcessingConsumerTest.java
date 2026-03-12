package com.fiap.fiapx.processing.adapters.driver.api.consumer;

import com.fiap.fiapx.processing.adapters.driver.api.dto.request.VideoProcessingMessage;
import com.fiap.fiapx.processing.core.application.usecases.ProcessVideoUseCase;
import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import com.fiap.fiapx.processing.core.domain.exception.ProcessingException;
import com.fiap.fiapx.processing.core.domain.model.VideoProcessingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VideoProcessingConsumerTest {

    @Mock
    private ProcessVideoUseCase processVideoUseCase;

    private VideoProcessingConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new VideoProcessingConsumer(processVideoUseCase);
    }

    @Test
    void shouldConvertMessageToDomainRequestAndInvokeUseCase() {
        VideoProcessingMessage message = new VideoProcessingMessage(
                "video-123",
                "/tmp/test-video.mp4",
                null,
                "user-456",
                VideoFormat.MP4
        );

        consumer.processVideoMessage(message, 1L);

        ArgumentCaptor<VideoProcessingRequest> captor = ArgumentCaptor.forClass(VideoProcessingRequest.class);
        verify(processVideoUseCase).execute(captor.capture());

        VideoProcessingRequest request = captor.getValue();
        assertEquals("video-123", request.videoId());
        assertEquals("/tmp/test-video.mp4", request.inputPath().toString());
        assertEquals(VideoProcessingMessage.DEFAULT_FRAME_INTERVAL_SECONDS, request.frameIntervalSeconds());
        assertEquals(VideoFormat.MP4, request.format());
        assertEquals("user-456", request.userId());
    }

    @Test
    void shouldUseSystemUserWhenUserIdIsNull() {
        VideoProcessingMessage message = new VideoProcessingMessage(
                "video-123",
                "/tmp/test-video.mp4",
                2.0,
                null,
                VideoFormat.MP4
        );

        consumer.processVideoMessage(message, 1L);

        ArgumentCaptor<VideoProcessingRequest> captor = ArgumentCaptor.forClass(VideoProcessingRequest.class);
        verify(processVideoUseCase).execute(captor.capture());

        VideoProcessingRequest request = captor.getValue();
        assertEquals("system", request.userId());
        assertEquals(2.0, request.frameIntervalSeconds());
    }

    @Test
    void shouldPropagateIllegalArgumentExceptionWithoutWrapping() {
        VideoProcessingMessage message = new VideoProcessingMessage(
                "video-123",
                "/tmp/test-video.mp4",
                1.0,
                "user-456",
                VideoFormat.MP4
        );

        doThrow(new IllegalArgumentException("invalid"))
                .when(processVideoUseCase).execute(any(VideoProcessingRequest.class));

        assertThrows(IllegalArgumentException.class, () ->
                consumer.processVideoMessage(message, 1L)
        );
    }

    @Test
    void shouldWrapUnexpectedExceptionInProcessingException() {
        VideoProcessingMessage message = new VideoProcessingMessage(
                "video-123",
                "/tmp/test-video.mp4",
                1.0,
                "user-456",
                VideoFormat.MP4
        );

        doThrow(new RuntimeException("boom"))
                .when(processVideoUseCase).execute(any(VideoProcessingRequest.class));

        ProcessingException ex = assertThrows(ProcessingException.class, () ->
                consumer.processVideoMessage(message, 1L)
        );

        assertEquals("Failed to process video: video-123", ex.getMessage());
    }
}

