package com.fiap.fiapx.processing.core.domain.model;

import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;

import java.nio.file.Path;

/**
 * Requisição de processamento de vídeo.
 */
public record VideoProcessingRequest(
    String videoId,
    Path inputPath,
    double frameIntervalSeconds,
    VideoFormat format,
    String userId
) {
    public VideoProcessingRequest {
        if (videoId == null || videoId.isBlank()) {
            throw new IllegalArgumentException("Video ID cannot be null or blank");
        }
        if (inputPath == null) {
            throw new IllegalArgumentException("Input path cannot be null");
        }
        if (frameIntervalSeconds <= 0) {
            throw new IllegalArgumentException("Frame interval must be greater than zero: " + frameIntervalSeconds);
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or blank");
        }
    }
}
