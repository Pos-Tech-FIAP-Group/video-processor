package com.fiap.fiapx.video.adapters.driven.infra.messaging.messages;

import java.util.UUID;

public record VideoProcessingCompletedMessage(
        UUID videoId,
        boolean success,
        Integer frameCount,
        String zipPath,
        String errorMessage
) {}
