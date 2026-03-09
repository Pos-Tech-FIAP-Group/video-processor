package com.fiap.fiapx.video.core.application.ports;

import java.util.UUID;

public interface PublishVideoProcessingRequestedPort {
    void publish(UUID videoId, UUID userId, String videoPath, Double frameIntervalSeconds);
}