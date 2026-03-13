package com.fiap.fiapx.processing.core.application.usecases;

import com.fiap.fiapx.processing.core.application.ports.*;
import com.fiap.fiapx.processing.core.domain.exception.ProcessingException;
import com.fiap.fiapx.processing.core.domain.model.ProcessingResult;
import com.fiap.fiapx.processing.core.domain.model.VideoProcessingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Use case principal para processamento de vídeo.
 * Recebe requisição, valida parâmetros, processa e publica evento na fila
 * video.processing.completed.processing-service (sucesso) ou evento de falha.
 * Não persiste estado localmente.
 */
@Component
public class ProcessVideoUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ProcessVideoUseCase.class);

    private final VideoMetadataPort videoMetadataPort;
    private final VideoFormatDetectorPort formatDetectorPort;
    private final VideoProcessingStrategyResolverPort strategyResolver;
    private final ProcessingEventPublisherPort eventPublisherPort;
    private final ZipStorageUploadPort zipStorageUploadPort;

    public ProcessVideoUseCase(
            VideoMetadataPort videoMetadataPort,
            VideoFormatDetectorPort formatDetectorPort,
            VideoProcessingStrategyResolverPort strategyResolver,
            ProcessingEventPublisherPort eventPublisherPort,
            ZipStorageUploadPort zipStorageUploadPort) {
        this.videoMetadataPort = videoMetadataPort;
        this.formatDetectorPort = formatDetectorPort;
        this.strategyResolver = strategyResolver;
        this.eventPublisherPort = eventPublisherPort;
        this.zipStorageUploadPort = zipStorageUploadPort;
    }

    /**
     * Executa o processamento do vídeo: valida intervalo de frames,
     * detecta formato, processa com a strategy e publica evento de conclusão na fila.
     */
    public void execute(VideoProcessingRequest request) {
        String videoId = request.videoId();
        MDC.put("videoId", videoId);

        try {
            logger.info("Starting video processing for videoId: {}", videoId);

            validateFrameInterval(request);
            VideoProcessingRequest requestWithFormat = detectAndSetFormat(request);
            ProcessingResult result = processVideoWithStrategy(requestWithFormat);

            String resultLocation = zipStorageUploadPort
                    .uploadAndGetPublicUrl(result.zipPath(), request.userId(), videoId)
                    .orElse(result.resultLocation());

            eventPublisherPort.publishProcessingCompleted(
                    videoId,
                    resultLocation,
                    result.frameCount()
            );

            logger.info("Video processing completed successfully for videoId: {}", videoId);

        } catch (Exception e) {
            logger.error("Video processing failed for videoId: {}", videoId, e);
            eventPublisherPort.publishProcessingFailed(videoId, request.userId(), e.getMessage());

            if (e instanceof IllegalArgumentException || e instanceof ProcessingException) {
                throw e;
            }

            throw new ProcessingException("Failed to process video", e);
        } finally {
            MDC.remove("videoId");
        }
    }

    private void validateFrameInterval(VideoProcessingRequest request) {
        Path videoPath = request.inputPath();
        var duration = videoMetadataPort.getDuration(videoPath);

        if (request.frameIntervalSeconds() > duration.seconds()) {
            throw new IllegalArgumentException(
                    String.format("Frame interval (%.2f) cannot be greater than video duration (%.2f seconds)",
                            request.frameIntervalSeconds(), duration.seconds()));
        }

        logger.debug("Frame interval validation passed: {} <= {}",
                request.frameIntervalSeconds(), duration.seconds());
    }

    private VideoProcessingRequest detectAndSetFormat(VideoProcessingRequest request) {
        var detectedFormat = formatDetectorPort.detectFormat(request.inputPath());

        if (detectedFormat == null && request.format() == null) {
            throw new IllegalArgumentException("Could not detect video format and no format provided");
        }

        if (request.format() == null) {
            logger.info("Detected video format: {} for videoId: {}", detectedFormat, request.videoId());
            return new VideoProcessingRequest(
                    request.videoId(),
                    request.inputPath(),
                    request.frameIntervalSeconds(),
                    detectedFormat,
                    request.userId()
            );
        }

        return request;
    }

    private ProcessingResult processVideoWithStrategy(VideoProcessingRequest request) {
        var strategy = strategyResolver.getStrategy(request.format());
        logger.info("Using strategy: {} for format: {}",
                strategy.getClass().getSimpleName(), request.format());

        return strategy.processVideo(request);
    }
}
