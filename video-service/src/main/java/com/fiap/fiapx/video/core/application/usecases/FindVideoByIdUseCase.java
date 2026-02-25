package com.fiap.fiapx.video.core.application.usecases;

import com.fiap.fiapx.video.core.domain.model.Video;

import java.util.UUID;

public interface FindVideoByIdUseCase {
    Video execute(UUID id);
}