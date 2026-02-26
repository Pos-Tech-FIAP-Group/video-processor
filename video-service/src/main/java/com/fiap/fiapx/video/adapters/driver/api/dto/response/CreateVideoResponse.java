package com.fiap.fiapx.video.adapters.driver.api.dto.response;

import com.fiap.fiapx.video.core.domain.enums.VideoStatus;

import java.time.Instant;
import java.util.UUID;

public record CreateVideoResponse(
        UUID id,
        VideoStatus status,
        Instant createdAt
) {}