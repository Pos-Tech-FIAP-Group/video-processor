package com.fiap.fiapx.processing.adapters.driver.api.dto.request;

import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO de mensagem recebida da fila para processamento de vídeo.
 */
public record VideoProcessingMessage(
    @NotBlank(message = "Video ID is required")
    String videoId,
    
    @NotBlank(message = "Input location is required")
    String inputLocation,
    
    @NotNull(message = "Frame interval seconds is required")
    @Positive(message = "Frame interval must be positive")
    Double frameIntervalSeconds,
    
    String userId,
    
    VideoFormat format
) {
}
