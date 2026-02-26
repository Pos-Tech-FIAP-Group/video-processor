package com.fiap.fiapx.video.core.application.ports;

import com.fiap.fiapx.video.core.application.common.PageResult;
import com.fiap.fiapx.video.core.domain.model.Video;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoRepositoryPort {
    Video save(Video video);
    Optional<Video> findById(UUID id);
    PageResult<Video> findByUserId(UUID userId, int page, int size);
}