package com.fiap.fiapx.processing.adapters.driver.api.dto.response;

/**
 * Response after extracting frames from a video.
 */
public record ExtractFramesResponse(
        String zipPath
) {}