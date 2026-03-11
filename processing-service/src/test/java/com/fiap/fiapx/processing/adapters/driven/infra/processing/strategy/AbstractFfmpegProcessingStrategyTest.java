package com.fiap.fiapx.processing.adapters.driven.infra.processing.strategy;

import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import com.fiap.fiapx.processing.core.domain.model.ProcessingResult;
import com.fiap.fiapx.processing.core.domain.model.VideoProcessingRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class AbstractFfmpegProcessingStrategyTest {

    private static class TestStrategy extends AbstractFfmpegProcessingStrategy {

        TestStrategy(Path zipsDirectory) {
            super(zipsDirectory);
        }

        @Override
        protected VideoFormat getSupportedFormat() {
            return VideoFormat.MP4;
        }

        @Override
        protected void extractFramesWithFfmpeg(Path videoPath, Path outputDir, double frameIntervalSeconds) throws IOException {
            // Simula o FFmpeg gerando três frames PNG
            Files.createFile(outputDir.resolve("frame_0001.png"));
            Files.createFile(outputDir.resolve("frame_0002.png"));
            Files.createFile(outputDir.resolve("frame_0003.png"));
        }
    }

    private static class FailingStrategy extends AbstractFfmpegProcessingStrategy {

        FailingStrategy(Path zipsDirectory) {
            super(zipsDirectory);
        }

        @Override
        protected VideoFormat getSupportedFormat() {
            return VideoFormat.MP4;
        }

        @Override
        protected void extractFramesWithFfmpeg(Path videoPath, Path outputDir, double frameIntervalSeconds) throws IOException {
            throw new IOException("Simulated FFmpeg failure");
        }
    }

    @Test
    void shouldProcessVideoAndCreateZipWithFrames() throws IOException {
        Path tempVideo = Files.createTempFile("video-", ".mp4");
        Path zipsDir = Files.createTempDirectory("zips-");

        try {
            VideoProcessingRequest request = new VideoProcessingRequest(
                    "video-123",
                    tempVideo,
                    1.0,
                    VideoFormat.MP4,
                    "user-456"
            );

            TestStrategy strategy = new TestStrategy(zipsDir);

            ProcessingResult result = strategy.processVideo(request);

            assertNotNull(result);
            assertEquals(3L, result.frameCount());
            assertTrue(Files.exists(result.zipPath()));
            assertEquals(result.zipPath().toAbsolutePath().toString(), result.resultLocation());

            assertTrue(strategy.supports(VideoFormat.MP4));
            assertFalse(strategy.supports(VideoFormat.AVI));
            assertFalse(strategy.supports(null));
        } finally {
            Files.deleteIfExists(tempVideo);
            if (Files.exists(zipsDir)) {
                try (var paths = Files.walk(zipsDir).sorted(Comparator.reverseOrder())) {
                    paths.forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
                }
            }
        }
    }

    @Test
    void shouldThrowWhenVideoFileDoesNotExist() throws IOException {
        Path nonExisting = Paths.get("does-not-exist-" + System.nanoTime() + ".mp4");
        Path zipsDir = Files.createTempDirectory("zips-");

        try {
            VideoProcessingRequest request = new VideoProcessingRequest(
                    "video-123",
                    nonExisting,
                    1.0,
                    VideoFormat.MP4,
                    "user-456"
            );

            TestStrategy strategy = new TestStrategy(zipsDir);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> strategy.processVideo(request));

            assertTrue(ex.getMessage().contains("Video path must point to an existing file"));
        } finally {
            if (Files.exists(zipsDir)) {
                try (var paths = Files.walk(zipsDir).sorted(Comparator.reverseOrder())) {
                    paths.forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
                }
            }
        }
    }

    @Test
    void shouldWrapIOExceptionFromExtractFrames() throws IOException {
        Path tempVideo = Files.createTempFile("video-", ".mp4");
        Path zipsDir = Files.createTempDirectory("zips-");

        try {
            VideoProcessingRequest request = new VideoProcessingRequest(
                    "video-123",
                    tempVideo,
                    1.0,
                    VideoFormat.MP4,
                    "user-456"
            );

            FailingStrategy strategy = new FailingStrategy(zipsDir);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> strategy.processVideo(request));

            assertTrue(ex.getMessage().contains("Failed to process video"));
        } finally {
            Files.deleteIfExists(tempVideo);
            if (Files.exists(zipsDir)) {
                try (var paths = Files.walk(zipsDir).sorted(Comparator.reverseOrder())) {
                    paths.forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
                }
            }
        }
    }
}

