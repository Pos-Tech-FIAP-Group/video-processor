package com.fiap.fiapx.processing.adapters.driver.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for the extract-frames endpoint.
 * videoPath is relative to the configured base path (e.g. /data): use "video.mp4", "video1/video.mp4", etc.
 * frameIntervalSeconds is optional; default 1.0 (one frame per second).
 */
public record ExtractFramesRequest(

        @NotBlank(message = "videoPath is required")
        String videoPath,

        @DecimalMin(value = "0.001", message = "frameIntervalSeconds must be greater than 0")
        Double frameIntervalSeconds
) {
    /**
     * Interval between extracted frames in seconds. Default 1.0 if not provided.
     */
    public double frameIntervalSecondsOrDefault() {
        return frameIntervalSeconds != null ? frameIntervalSeconds : 1.0;
    }
}
