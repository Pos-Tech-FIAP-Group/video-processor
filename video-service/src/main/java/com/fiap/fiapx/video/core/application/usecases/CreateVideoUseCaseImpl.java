package com.fiap.fiapx.video.core.application.usecases;

import com.fiap.fiapx.video.core.application.ports.VideoRepositoryPort;
import com.fiap.fiapx.video.core.application.usecases.command.CreateVideoCommand;
import com.fiap.fiapx.video.core.domain.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateVideoUseCaseImpl implements CreateVideoUseCase {

    private final VideoRepositoryPort repository;

    @Override
    public Video execute(CreateVideoCommand command) {
        Video video = Video.create(
                command.userId(),
                command.originalFilename(),
                command.contentType(),
                command.videoPath()
        );

        return repository.save(video);
    }
}