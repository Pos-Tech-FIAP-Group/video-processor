package com.fiap.fiapx.video.core.application.usecases.command;

import java.util.UUID;

public record CreateVideoCommand(
        UUID userId,
        String originalFilename,
        String contentType,
        String videoPath,
        Double frameIntervalSeconds
) {
}