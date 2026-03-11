package com.fiap.fiapx.processing.core.domain.model;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProcessingResultTest {

    private static final Path VALID_ZIP_PATH = Paths.get("/tmp/result.zip");

    @Test
    void shouldCreateValidProcessingResult() {
        assertDoesNotThrow(() -> new ProcessingResult(
                VALID_ZIP_PATH,
                10L,
                "/tmp/result.zip"
        ));
    }

    @Test
    void shouldThrowWhenZipPathIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new ProcessingResult(
                null,
                10L,
                "/tmp/result.zip"
        ));
    }

    @Test
    void shouldThrowWhenFrameCountIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new ProcessingResult(
                VALID_ZIP_PATH,
                -1L,
                "/tmp/result.zip"
        ));
    }

    @Test
    void shouldThrowWhenResultLocationIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new ProcessingResult(
                VALID_ZIP_PATH,
                10L,
                null
        ));
    }

    @Test
    void shouldThrowWhenResultLocationIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new ProcessingResult(
                VALID_ZIP_PATH,
                10L,
                "   "
        ));
    }
}

