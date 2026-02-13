package com.fiap.fiapx.processing.adapters.driver.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for the extract-frames endpoint.
 */
public record ExtractFramesRequest(

        @NotBlank(message = "videoPath is required")
        String videoPath
) {}
