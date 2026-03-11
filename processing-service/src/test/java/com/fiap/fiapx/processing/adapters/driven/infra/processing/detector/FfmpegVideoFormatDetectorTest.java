package com.fiap.fiapx.processing.adapters.driven.infra.processing.detector;

import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FfmpegVideoFormatDetectorTest {

    private final FfmpegVideoFormatDetector detector = new FfmpegVideoFormatDetector();

    @Test
    void shouldDetectFormatFromMp4Extension() throws IOException {
        Path tempFile = Files.createTempFile("video-format-test-", ".mp4");
        try {
            VideoFormat format = detector.detectFormat(tempFile);
            assertEquals(VideoFormat.MP4, format);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void shouldReturnNullForUnknownExtension() throws IOException {
        Path tempFile = Files.createTempFile("video-format-test-", ".unknown");
        try {
            VideoFormat format = detector.detectFormat(tempFile);
            assertNull(format);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void shouldReturnNullWhenPathIsNullOrDoesNotExist() {
        assertNull(detector.detectFormat(null));

        Path nonExisting = Path.of("non-existing-video-file-12345.mp4");
        assertNull(detector.detectFormat(nonExisting));
    }
}

