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

    private static final double DEFAULT_FRAME_INTERVAL_SECONDS = 1.0;

    @Override
    public Video execute(CreateVideoCommand command) {
        Video video = Video.create(
                command.userId(),
                command.originalFilename(),
                command.contentType(),
                command.videoPath()
        );

        repository.save(video);

        double interval = command.frameIntervalSeconds() != null && command.frameIntervalSeconds() > 0
                ? command.frameIntervalSeconds()
                : DEFAULT_FRAME_INTERVAL_SECONDS;

        try {
            publishPort.publish(video.getId(), video.getUserId(), video.getVideoPath(), interval);
        } catch (Exception ex) {
            var failed = video.failProcessing("Falha ao solicitar processamento: " + ex.getMessage());
            repository.save(failed);
            throw ex;
        }
        return video;
    }
}