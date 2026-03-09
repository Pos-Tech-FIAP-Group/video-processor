package com.fiap.fiapx.video.adapters.driver.api.mapper;

import com.fiap.fiapx.video.adapters.driver.api.dto.response.CreateVideoResponse;
import com.fiap.fiapx.video.adapters.driver.api.dto.response.VideoResponse;
import com.fiap.fiapx.video.core.domain.model.Video;

public final class VideoApiMapper {

    public static CreateVideoResponse toCreateResponse(Video video) {
        return new CreateVideoResponse(video.getId(), video.getStatus(), video.getCreatedAt());
    }

    public static VideoResponse toResponse(Video video) {
        return new VideoResponse(
                video.getId(),
                video.getUserId(),
                video.getOriginalFilename(),
                video.getContentType(),
                video.getVideoPath(),
                video.getZipPath(),
                video.getStatus(),
                video.getFrameCount(),
                video.getErrorMessage(),
                video.getCreatedAt(),
                video.getUpdatedAt(),
                video.getProcessedAt()
        );
    }
    }