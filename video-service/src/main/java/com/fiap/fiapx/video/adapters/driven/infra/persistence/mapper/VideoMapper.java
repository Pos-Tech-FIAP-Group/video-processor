package com.fiap.fiapx.video.adapters.driven.infra.persistence.mapper;

import com.fiap.fiapx.video.adapters.driven.infra.persistence.entity.VideoEntity;
import com.fiap.fiapx.video.core.domain.model.Video;

public final class VideoMapper {

    private VideoMapper() {}

    public static VideoEntity toEntity(Video v) {
        VideoEntity e = new VideoEntity();
        e.setId(v.getId());
        e.setUserId(v.getUserId());
        e.setOriginalFilename(v.getOriginalFilename());
        e.setContentType(v.getContentType());
        e.setVideoPath(v.getVideoPath());
        e.setZipPath(v.getZipPath());
        e.setStatus(v.getStatus());
        e.setFrameCount(v.getFrameCount());
        e.setErrorMessage(v.getErrorMessage());
        e.setCreatedAt(v.getCreatedAt());
        e.setUpdatedAt(v.getUpdatedAt());
        e.setProcessedAt(v.getProcessedAt());
        return e;
    }

    public static Video toDomain(VideoEntity e) {
        return new Video(
                e.getId(),
                e.getUserId(),
                e.getOriginalFilename(),
                e.getContentType(),
                e.getVideoPath(),
                e.getZipPath(),
                e.getStatus(),
                e.getFrameCount(),
                e.getErrorMessage(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getProcessedAt()
        );
    }
}