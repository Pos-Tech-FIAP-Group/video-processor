package com.fiap.fiapx.video.core.application.usecases;

import com.fiap.fiapx.video.core.application.common.PageResult;
import com.fiap.fiapx.video.core.domain.model.Video;

import java.util.UUID;

public interface ListVideosByUserUseCase {

    PageResult<Video> execute(UUID userId, int page, int size);
}