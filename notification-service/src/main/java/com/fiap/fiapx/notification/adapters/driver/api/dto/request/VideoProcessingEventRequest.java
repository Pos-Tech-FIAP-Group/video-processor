package com.fiap.fiapx.notification.adapters.driver.api.dto.request;

public record VideoProcessingEventRequest(
    String videoId,
    boolean success,
    Integer frameCount,
    String zipPath,
    String errorMessage
) {
}