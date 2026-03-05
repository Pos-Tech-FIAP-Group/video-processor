package com.fiap.fiapx.video.adapters.driven.infra.metrics;

import com.fiap.fiapx.video.core.application.ports.VideoProcessingMetricsPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class MicrometerVideoProcessingMetricsAdapter implements VideoProcessingMetricsPort {

    private final Counter videoProcessingRequestsTotal;
    private final Counter videoProcessingSuccessTotal;
    private final Counter videoProcessingFailureTotal;
    private final Timer videoProcessingDurationSeconds;

    public MicrometerVideoProcessingMetricsAdapter(MeterRegistry registry) {
        this.videoProcessingRequestsTotal = Counter.builder("video_processing_requests_total")
                .description("Total de vídeos recebidos para processamento")
                .register(registry);

        this.videoProcessingSuccessTotal = Counter.builder("video_processing_success_total")
                .description("Total de vídeos processados com sucesso")
                .register(registry);

        this.videoProcessingFailureTotal = Counter.builder("video_processing_failure_total")
                .description("Total de vídeos com falha de processamento")
                .register(registry);

        this.videoProcessingDurationSeconds = Timer.builder("video_processing_duration_seconds")
                .description("Duração do processamento de vídeos em segundos")
                .register(registry);
    }

    @Override
    public void incrementRequests() {
        videoProcessingRequestsTotal.increment();
    }

    @Override
    public void incrementSuccess() {
        videoProcessingSuccessTotal.increment();
    }

    @Override
    public void incrementFailure() {
        videoProcessingFailureTotal.increment();
    }

    @Override
    public void recordProcessingDuration(Duration duration) {
        if (duration != null && !duration.isNegative() && !duration.isZero()) {
            videoProcessingDurationSeconds.record(duration);
        }
    }
}

