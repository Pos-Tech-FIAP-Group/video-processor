package com.fiap.fiapx.video.adapters.driven.infra.messaging.messages;

import java.util.UUID;

public record VideoProcessingRequestedMessage(
        UUID videoId,
        UUID userId,
        String videoPath
) {}