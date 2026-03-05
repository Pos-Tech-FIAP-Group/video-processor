package com.fiap.fiapx.video.core.application.usecases;

import com.fiap.fiapx.video.core.application.exceptions.VideoNotFoundException;
import com.fiap.fiapx.video.core.application.ports.VideoProcessingMetricsPort;
import com.fiap.fiapx.video.core.application.ports.VideoRepositoryPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@AllArgsConstructor
@Service
public class UpdateVideoProcessingResultUseCaseImpl implements UpdateVideoProcessingResultUseCase {

    private final VideoRepositoryPort videoRepositoryPort;
    private final VideoProcessingMetricsPort metricsPort;


    @Override
    public void executeSuccess(UUID videoId, Integer frameCount, String zipPath) {
        var video = videoRepositoryPort.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException(videoId));

        var processed = video.completeProcessing(frameCount, zipPath);
        videoRepositoryPort.save(processed);

        metricsPort.incrementSuccess();

        if (video.getCreatedAt() != null && processed.getProcessedAt() != null) {
            var duration = Duration.between(video.getCreatedAt(), processed.getProcessedAt());
            metricsPort.recordProcessingDuration(duration);
        }
    }

    @Override
    public void executeFailure(UUID videoId, String errorMessage) {
        var video = videoRepositoryPort.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException(videoId));

        var failed = video.failProcessing(errorMessage);
        videoRepositoryPort.save(failed);

        metricsPort.incrementFailure();
    }
}
