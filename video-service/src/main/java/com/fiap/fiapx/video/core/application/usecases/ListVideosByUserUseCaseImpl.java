package com.fiap.fiapx.video.core.application.usecases;

import com.fiap.fiapx.video.core.application.common.PageResult;
import com.fiap.fiapx.video.core.application.ports.VideoRepositoryPort;
import com.fiap.fiapx.video.core.domain.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Service
@RequiredArgsConstructor
public class ListVideosByUserUseCaseImpl implements ListVideosByUserUseCase {

    private final VideoRepositoryPort videoRepositoryPort;

    @Override
    public PageResult<Video> execute(UUID userId, int page, int size) {
        return videoRepositoryPort.findByUserId(userId, page, size);
    }
}