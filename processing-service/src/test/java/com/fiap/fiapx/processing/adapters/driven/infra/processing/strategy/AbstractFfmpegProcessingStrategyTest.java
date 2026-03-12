package com.fiap.fiapx.processing.adapters.driven.infra.processing.strategy;

import com.fiap.fiapx.processing.adapters.driven.infra.processing.runner.ProcessRunner;
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

    private static class FakeProcessRunner implements ProcessRunner {

        @Override
        public ProcessResult run(String... command) throws IOException {
            // Último argumento é o padrão de saída dos frames (frame_%04d.png)
            String outputPattern = command[command.length - 1];
            Path patternPath = Paths.get(outputPattern);
            Path dir = patternPath.getParent();
            Files.createDirectories(dir);
            Files.createFile(dir.resolve("frame_0001.png"));
            Files.createFile(dir.resolve("frame_0002.png"));
            Files.createFile(dir.resolve("frame_0003.png"));
            return new ProcessResult(0, "");
        }
    }

    private static class ExitCodeProcessRunner implements ProcessRunner {

        private final int exitCode;

        ExitCodeProcessRunner(int exitCode) {
            this.exitCode = exitCode;
        }

        @Override
        public ProcessResult run(String... command) {
            return new ProcessResult(exitCode, "");
        }
    }

    private static class InterruptingProcessRunner implements ProcessRunner {

        @Override
        public ProcessResult run(String... command) throws InterruptedException {
            throw new InterruptedException("interrupted");
        }
    }

    private static class TestStrategy extends AbstractFfmpegProcessingStrategy {

        TestStrategy(Path zipsDirectory, ProcessRunner processRunner) {
            super(zipsDirectory, processRunner);
        }

        @Override
        protected VideoFormat getSupportedFormat() {
            return VideoFormat.MP4;
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

            TestStrategy strategy = new TestStrategy(zipsDir, new FakeProcessRunner());

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

            TestStrategy strategy = new TestStrategy(zipsDir, new FakeProcessRunner());

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

            TestStrategy strategy = new TestStrategy(zipsDir, new ExitCodeProcessRunner(1));

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

    @Test
    void shouldWrapInterruptedExceptionFromFfmpeg() throws IOException {
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

            TestStrategy strategy = new TestStrategy(zipsDir, new InterruptingProcessRunner());

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

