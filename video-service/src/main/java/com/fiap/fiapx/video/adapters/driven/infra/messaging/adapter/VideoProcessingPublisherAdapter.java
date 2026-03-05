package com.fiap.fiapx.video.adapters.driven.infra.messaging.adapter;

import com.fiap.fiapx.video.adapters.driven.infra.messaging.config.RabbitMqConfig;
import com.fiap.fiapx.video.adapters.driven.infra.messaging.messages.VideoProcessingRequestedMessage;
import com.fiap.fiapx.video.core.application.ports.PublishVideoProcessingRequestedPort;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@AllArgsConstructor
@Component
public class VideoProcessingPublisherAdapter implements PublishVideoProcessingRequestedPort {

    private static final Logger logger = LoggerFactory.getLogger(VideoProcessingPublisherAdapter.class);

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(UUID videoId, UUID userId, String videoPath) {
        var message = new VideoProcessingRequestedMessage(videoId, userId, videoPath);
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.RK_REQUESTED, message);
        logger.info("video_processing_requested videoId={} userId={} videoPath={}", videoId, userId, videoPath);
    }
}
