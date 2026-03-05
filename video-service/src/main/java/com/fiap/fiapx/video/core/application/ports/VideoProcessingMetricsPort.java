package com.fiap.fiapx.video.core.application.ports;

import java.time.Duration;

public interface VideoProcessingMetricsPort {

    void incrementRequests();

    void incrementSuccess();

    void incrementFailure();

    void recordProcessingDuration(Duration duration);
}

