package com.fiap.fiapx.video.adapters.driven.infra.messaging.adapter;


import com.fiap.fiapx.video.adapters.driven.infra.messaging.config.RabbitMqConfig;
import com.fiap.fiapx.video.adapters.driven.infra.messaging.messages.VideoProcessingCompletedMessage;
import com.fiap.fiapx.video.core.application.usecases.UpdateVideoProcessingResultUseCase;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class VideoProcessingResultListener {

    private static final Logger logger = LoggerFactory.getLogger(VideoProcessingResultListener.class);

    private final UpdateVideoProcessingResultUseCase useCase;

    @RabbitListener(queues = RabbitMqConfig.QUEUE_COMPLETED_VIDEO_SERVICE)
    public void onMessage(VideoProcessingCompletedMessage message) {

        if (message.success()) {
            logger.info("video_processing_completed_success videoId={} frameCount={} zipPath={}",
                    message.videoId(), message.frameCount(), message.zipPath());
            useCase.executeSuccess(message.videoId(), message.frameCount(), message.zipPath());
            return;
        }

        logger.warn("video_processing_completed_failure videoId={} errorMessage={}",
                message.videoId(), message.errorMessage());
        useCase.executeFailure(message.videoId(),
                message.errorMessage() != null ? message.errorMessage() : "Erro desconhecido no processamento");
    }
}