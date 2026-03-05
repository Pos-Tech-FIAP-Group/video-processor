package com.fiap.fiapx.processing.adapters.driver.api.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO de mensagem recebida da fila para processamento de vídeo.
 * Aceita "videoPath" (video-service) ou "inputLocation" como caminho do arquivo.
 */
public record VideoProcessingMessage(
    @NotBlank(message = "Video ID is required")
    String videoId,
    
    @NotBlank(message = "Input location or videoPath is required")
    @JsonAlias("videoPath")
    String inputLocation,
    
    /** Intervalo em segundos entre frames; default 1.0 se não enviado (ex.: pelo video-service). */
    @PositiveOrZero(message = "Frame interval must be positive or zero")
    Double frameIntervalSeconds,
    
    String userId,
    
    VideoFormat format
) {
    /** Intervalo padrão quando o publicador não envia (ex.: video-service). */
    public static final double DEFAULT_FRAME_INTERVAL_SECONDS = 1.0;

    public double effectiveFrameIntervalSeconds() {
        return frameIntervalSeconds != null && frameIntervalSeconds > 0
            ? frameIntervalSeconds
            : DEFAULT_FRAME_INTERVAL_SECONDS;
    }
}
