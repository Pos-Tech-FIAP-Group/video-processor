package com.fiap.fiapx.processing.core.application.usecases;

import com.fiap.fiapx.processing.core.application.ports.*;
import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import com.fiap.fiapx.processing.core.domain.model.ProcessingResult;
import com.fiap.fiapx.processing.core.domain.model.VideoDuration;
import com.fiap.fiapx.processing.core.domain.model.VideoProcessingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessVideoUseCaseTest {

    @Mock
    private VideoMetadataPort videoMetadataPort;

    @Mock
    private VideoFormatDetectorPort formatDetectorPort;

    @Mock
    private VideoProcessingStrategyPort strategyPort;

    @Mock
    private VideoProcessingStrategyResolverPort strategyResolver;

    @Mock
    private ProcessingEventPublisherPort eventPublisherPort;

    @Mock
    private ZipStorageUploadPort zipStorageUploadPort;

    @InjectMocks
    private ProcessVideoUseCase processVideoUseCase;

    private VideoProcessingRequest request;
    private Path videoPath;

    @BeforeEach
    void setUp() {
        videoPath = Paths.get("/tmp/test-video.mp4");
        request = new VideoProcessingRequest(
                "video-123",
                videoPath,
                1.0,
                VideoFormat.MP4,
                "user-456"
        );
    }

    @Test
    void shouldProcessVideoSuccessfully() {
        VideoDuration duration = new VideoDuration(10.0);
        ProcessingResult result = new ProcessingResult(
                Paths.get("/tmp/result.zip"),
                10L,
                "/tmp/result.zip"
        );

        when(videoMetadataPort.getDuration(videoPath)).thenReturn(duration);
        when(formatDetectorPort.detectFormat(videoPath)).thenReturn(VideoFormat.MP4);
        when(strategyResolver.getStrategy(VideoFormat.MP4)).thenReturn(strategyPort);
        when(strategyPort.processVideo(any())).thenReturn(result);
        when(zipStorageUploadPort.uploadAndGetPublicUrl(any(), any(), any())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> processVideoUseCase.execute(request));

        verify(videoMetadataPort).getDuration(videoPath);
        verify(zipStorageUploadPort).uploadAndGetPublicUrl(eq(Paths.get("/tmp/result.zip")), eq("user-456"), eq("video-123"));
        verify(eventPublisherPort).publishProcessingCompleted("video-123", "/tmp/result.zip", 10L);
    }

    @Test
    void shouldPublishS3UrlWhenUploadReturnsUrl() {
        VideoDuration duration = new VideoDuration(10.0);
        Path zipPath = Paths.get("/tmp/result.zip");
        ProcessingResult result = new ProcessingResult(zipPath, 10L, "/tmp/result.zip");
        String s3Url = "https://my-bucket.s3.sa-east-1.amazonaws.com/user-456/video-123.zip";

        when(videoMetadataPort.getDuration(videoPath)).thenReturn(duration);
        when(formatDetectorPort.detectFormat(videoPath)).thenReturn(VideoFormat.MP4);
        when(strategyResolver.getStrategy(VideoFormat.MP4)).thenReturn(strategyPort);
        when(strategyPort.processVideo(any())).thenReturn(result);
        when(zipStorageUploadPort.uploadAndGetPublicUrl(eq(zipPath), eq("user-456"), eq("video-123")))
                .thenReturn(Optional.of(s3Url));

        assertDoesNotThrow(() -> processVideoUseCase.execute(request));

        verify(eventPublisherPort).publishProcessingCompleted("video-123", s3Url, 10L);
    }

    @Test
    void shouldFailWhenFrameIntervalGreaterThanDuration() {
        VideoDuration duration = new VideoDuration(5.0);
        VideoProcessingRequest invalidRequest = new VideoProcessingRequest(
                "video-123",
                videoPath,
                10.0, // frame interval > duration (5.0)
                VideoFormat.MP4,
                "user-456"
        );

        when(videoMetadataPort.getDuration(videoPath)).thenReturn(duration);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> processVideoUseCase.execute(invalidRequest)
        );

        assertTrue(exception.getMessage().contains("cannot be greater than video duration"));
        verify(eventPublisherPort).publishProcessingFailed(eq("video-123"), anyString());
    }

    @Test
    void shouldHandleProcessingFailure() {
        VideoDuration duration = new VideoDuration(10.0);
        RuntimeException processingError = new RuntimeException("FFmpeg failed");

        when(videoMetadataPort.getDuration(videoPath)).thenReturn(duration);
        when(formatDetectorPort.detectFormat(videoPath)).thenReturn(VideoFormat.MP4);
        when(strategyResolver.getStrategy(VideoFormat.MP4)).thenReturn(strategyPort);
        when(strategyPort.processVideo(any())).thenThrow(processingError);

        assertThrows(RuntimeException.class, () -> processVideoUseCase.execute(request));

        verify(eventPublisherPort).publishProcessingFailed(eq("video-123"), anyString());
    }
}
