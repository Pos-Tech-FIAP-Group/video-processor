package com.fiap.fiapx.processing.adapters.driven.infra.processing.metadata;

import com.fiap.fiapx.processing.core.domain.model.VideoDuration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FfmpegVideoMetadataAdapterTest {

    private final FfmpegVideoMetadataAdapter adapter = new FfmpegVideoMetadataAdapter();

    @Test
    void shouldThrowWhenPathIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adapter.getDuration(null));

        assertTrue(ex.getMessage().contains("Video path must exist"));
    }

    @Test
    void shouldThrowWhenPathDoesNotExist() {
        Path nonExisting = Path.of("non-existing-" + System.nanoTime() + ".mp4");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adapter.getDuration(nonExisting));

        assertTrue(ex.getMessage().contains("Video path must exist"));
    }

    @Test
    void shouldReturnDurationOrWrapFailureInRuntimeException() throws IOException {
        Path tempVideo = Files.createTempFile("video-metadata-", ".mp4");

        try {
            try {
                VideoDuration duration = adapter.getDuration(tempVideo);
                assertNotNull(duration);
                assertTrue(duration.seconds() >= 0.0);
            } catch (RuntimeException ex) {
                // Ambiente sem ffprobe ou falha na execução: apenas garante que a mensagem referencia o path
                assertTrue(ex.getMessage().contains(tempVideo.toString()));
            }
        } finally {
            Files.deleteIfExists(tempVideo);
        }
    }
}

