package com.fiap.fiapx.video.core.application.usecases;

import com.fiap.fiapx.video.core.application.exceptions.VideoNotFoundException;
import com.fiap.fiapx.video.core.application.ports.VideoRepositoryPort;
import com.fiap.fiapx.video.core.domain.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindVideoByIdUseCaseImpl implements FindVideoByIdUseCase {

    private final VideoRepositoryPort videoRepositoryPort;

    @Override
    public Video execute(UUID id) {
        return videoRepositoryPort.findById(id)
                .orElseThrow(() -> new VideoNotFoundException(id));
    }
}