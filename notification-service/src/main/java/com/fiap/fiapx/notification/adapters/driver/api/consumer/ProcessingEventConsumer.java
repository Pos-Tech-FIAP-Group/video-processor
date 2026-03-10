package com.fiap.fiapx.notification.adapters.driver.api.consumer;

import com.fiap.fiapx.notification.adapters.driver.api.dto.request.VideoProcessingEventRequest;
import com.fiap.fiapx.notification.core.application.usecases.HandleProcessingCompletedUseCase;
import com.fiap.fiapx.notification.core.application.usecases.HandleProcessingFailedUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

public class ProcessingEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ProcessingEventConsumer.class);

    private final HandleProcessingCompletedUseCase handleProcessingCompletedUseCase;
    private final HandleProcessingFailedUseCase handleProcessingFailedUseCase;

    public ProcessingEventConsumer(
        HandleProcessingCompletedUseCase handleProcessingCompletedUseCase,
        HandleProcessingFailedUseCase handleProcessingFailedUseCase
    ) {
        this.handleProcessingCompletedUseCase = handleProcessingCompletedUseCase;
        this.handleProcessingFailedUseCase = handleProcessingFailedUseCase;
    }

    @RabbitListener(
        queues = "${spring.rabbitmq.notification.queue:video.processing.notification-service.queue}",
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void consume(VideoProcessingEventRequest event) {
        logger.info("Received processing event. videoId={}, success={}", event.videoId(), event.success());

        if (event.success()) {
            handleProcessingCompletedUseCase.execute(
                event.videoId(),
                event.frameCount(),
                event.zipPath()
            );
            return;
        }

        handleProcessingFailedUseCase.execute(
            event.videoId(),
            event.errorMessage()
        );
    }
}