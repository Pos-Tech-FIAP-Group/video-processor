package com.fiap.fiapx.processing.core.domain.model;

import java.nio.file.Path;

/**
 * Resultado do processamento de um vídeo.
 */
public record ProcessingResult(
    Path zipPath,
    long frameCount,
    String resultLocation
) {
    public ProcessingResult {
        if (zipPath == null) {
            throw new IllegalArgumentException("Zip path cannot be null");
        }
        if (frameCount < 0) {
            throw new IllegalArgumentException("Frame count cannot be negative: " + frameCount);
        }
        if (resultLocation == null || resultLocation.isBlank()) {
            throw new IllegalArgumentException("Result location cannot be null or blank");
        }
    }
}
