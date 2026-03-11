package com.fiap.fiapx.processing.core.domain.model;

import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VideoProcessingRequestTest {

    private static final Path VALID_PATH = Paths.get("/tmp/video.mp4");

    @Test
    void shouldCreateRequestWithValidData() {
        assertDoesNotThrow(() -> new VideoProcessingRequest(
                "video-123",
                VALID_PATH,
                1.0,
                VideoFormat.MP4,
                "user-456"
        ));
    }

    @Test
    void shouldThrowWhenVideoIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new VideoProcessingRequest(
                null,
                VALID_PATH,
                1.0,
                VideoFormat.MP4,
                "user-456"
        ));
    }

    @Test
    void shouldThrowWhenVideoIdIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new VideoProcessingRequest(
                "  ",
                VALID_PATH,
                1.0,
                VideoFormat.MP4,
                "user-456"
        ));
    }

    @Test
    void shouldThrowWhenInputPathIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new VideoProcessingRequest(
                "video-123",
                null,
                1.0,
                VideoFormat.MP4,
                "user-456"
        ));
    }

    @Test
    void shouldThrowWhenFrameIntervalIsZero() {
        assertThrows(IllegalArgumentException.class, () -> new VideoProcessingRequest(
                "video-123",
                VALID_PATH,
                0.0,
                VideoFormat.MP4,
                "user-456"
        ));
    }

    @Test
    void shouldThrowWhenFrameIntervalIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new VideoProcessingRequest(
                "video-123",
                VALID_PATH,
                -1.0,
                VideoFormat.MP4,
                "user-456"
        ));
    }

    @Test
    void shouldThrowWhenUserIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new VideoProcessingRequest(
                "video-123",
                VALID_PATH,
                1.0,
                VideoFormat.MP4,
                null
        ));
    }

    @Test
    void shouldThrowWhenUserIdIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new VideoProcessingRequest(
                "video-123",
                VALID_PATH,
                1.0,
                VideoFormat.MP4,
                "   "
        ));
    }
}

