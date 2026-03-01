package com.fiap.fiapx.video.core.application.usecases;

import com.fiap.fiapx.video.core.application.ports.PublishVideoProcessingRequestedPort;
import com.fiap.fiapx.video.core.application.ports.VideoRepositoryPort;
import com.fiap.fiapx.video.core.application.usecases.command.CreateVideoCommand;
import com.fiap.fiapx.video.core.domain.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateVideoUseCaseImpl implements CreateVideoUseCase {

    private final VideoRepositoryPort repository;
    private final PublishVideoProcessingRequestedPort publishPort;

    @Override
    public void execute(CreateVideoCommand command) {
        Video video = Video.create(
                command.userId(),
                command.originalFilename(),
                command.contentType(),
                command.videoPath()
        );

        repository.save(video);

        try {
            publishPort.publish(video.getId(), video.getUserId(), video.getVideoPath());
        } catch (Exception ex) {
            var failed = video.failProcessing("Falha ao solicitar processamento: " + ex.getMessage());
            repository.save(failed);
            throw ex;
        }
    }
}