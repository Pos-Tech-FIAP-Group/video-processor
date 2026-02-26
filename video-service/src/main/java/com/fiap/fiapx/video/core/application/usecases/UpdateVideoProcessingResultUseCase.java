package com.fiap.fiapx.video.core.application.usecases;

import java.util.UUID;

public interface UpdateVideoProcessingResultUseCase {
    void executeSuccess(UUID videoId, Integer frameCount, String zipPath);
    void executeFailure(UUID videoId, String errorMessage);
}
