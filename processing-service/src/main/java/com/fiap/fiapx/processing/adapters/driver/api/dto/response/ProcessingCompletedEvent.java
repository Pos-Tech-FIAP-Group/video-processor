package com.fiap.fiapx.processing.adapters.driver.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * DTO de evento publicado quando o processamento é concluído com sucesso.
 */
public record ProcessingCompletedEvent(
    String videoId,
    String status,
    String resultLocation,
    Double frameIntervalSeconds,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime processedAt
) {
}
