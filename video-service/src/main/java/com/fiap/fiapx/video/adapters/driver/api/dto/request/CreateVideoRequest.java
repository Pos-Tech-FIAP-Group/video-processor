package com.fiap.fiapx.video.adapters.driver.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateVideoRequest(
        @NotBlank String originalFilename,
        @NotBlank String contentType,
        @NotBlank String videoPath
) {}