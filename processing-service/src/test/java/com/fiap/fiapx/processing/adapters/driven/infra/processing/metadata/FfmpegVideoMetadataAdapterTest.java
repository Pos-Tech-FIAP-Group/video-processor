package com.fiap.fiapx.processing.adapters.driven.infra.processing.metadata;

import com.fiap.fiapx.processing.adapters.driven.infra.processing.runner.ProcessRunner;
import com.fiap.fiapx.processing.core.domain.model.VideoDuration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FfmpegVideoMetadataAdapterTest {

    private static class StubProcessRunner implements ProcessRunner {

        private final int exitCode;
        private final String stdout;
        private final RuntimeException toThrow;

        StubProcessRunner(int exitCode, String stdout) {
            this(exitCode, stdout, null);
        }

        StubProcessRunner(int exitCode, String stdout, RuntimeException toThrow) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.toThrow = toThrow;
        }

        @Override
        public ProcessResult run(String... command) {
            if (toThrow != null) {
                throw toThrow;
            }
            return new ProcessResult(exitCode, stdout);
        }
    }

    @Test
    void shouldThrowWhenPathIsNull() {
        FfmpegVideoMetadataAdapter adapter = new FfmpegVideoMetadataAdapter(new StubProcessRunner(0, "10.0"));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adapter.getDuration(null));

        assertTrue(ex.getMessage().contains("Video path must exist"));
    }

    @Test
    void shouldThrowWhenPathDoesNotExist() {
        Path nonExisting = Path.of("non-existing-" + System.nanoTime() + ".mp4");

        FfmpegVideoMetadataAdapter adapter = new FfmpegVideoMetadataAdapter(new StubProcessRunner(0, "10.0"));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adapter.getDuration(nonExisting));

        assertTrue(ex.getMessage().contains("Video path must exist"));
    }

    @Test
    void shouldReturnDurationWhenOutputIsValid() throws IOException {
        Path tempVideo = Files.createTempFile("video-metadata-", ".mp4");

        try {
            FfmpegVideoMetadataAdapter adapter = new FfmpegVideoMetadataAdapter(
                    new StubProcessRunner(0, "42.5")
            );

            VideoDuration duration = adapter.getDuration(tempVideo);
            assertNotNull(duration);
            assertEquals(42.5, duration.seconds());
        } finally {
            Files.deleteIfExists(tempVideo);
        }
    }

    @Test
    void shouldWrapWhenExitCodeIsNonZero() throws IOException {
        Path tempVideo = Files.createTempFile("video-metadata-", ".mp4");

        try {
            FfmpegVideoMetadataAdapter adapter = new FfmpegVideoMetadataAdapter(
                    new StubProcessRunner(1, "")
            );

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> adapter.getDuration(tempVideo));

            assertTrue(ex.getMessage().contains("Failed to get video duration"));
        } finally {
            Files.deleteIfExists(tempVideo);
        }
    }

    @Test
    void shouldWrapWhenDurationStringIsEmpty() throws IOException {
        Path tempVideo = Files.createTempFile("video-metadata-", ".mp4");

        try {
            FfmpegVideoMetadataAdapter adapter = new FfmpegVideoMetadataAdapter(
                    new StubProcessRunner(0, "  ")
            );

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> adapter.getDuration(tempVideo));

            assertTrue(ex.getMessage().contains("Failed to get video duration"));
        } finally {
            Files.deleteIfExists(tempVideo);
        }
    }

    @Test
    void shouldWrapWhenDurationIsNotNumeric() throws IOException {
        Path tempVideo = Files.createTempFile("video-metadata-", ".mp4");

        try {
            FfmpegVideoMetadataAdapter adapter = new FfmpegVideoMetadataAdapter(
                    new StubProcessRunner(0, "not-a-number")
            );

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> adapter.getDuration(tempVideo));

            assertTrue(ex.getMessage().contains("Invalid duration format from FFprobe"));
        } finally {
            Files.deleteIfExists(tempVideo);
        }
    }
}
