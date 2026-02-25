package com.fiap.fiapx.video.adapters.driver.api.dto.response;

import com.fiap.fiapx.video.core.domain.enums.VideoStatus;

import java.time.Instant;
import java.util.UUID;

public record VideoResponse(
        UUID id,
        UUID userId,
        String originalFilename,
        String contentType,
        String videoPath,
        String zipPath,
        VideoStatus status,
        Integer frameCount,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt,
        Instant processedAt
) {}