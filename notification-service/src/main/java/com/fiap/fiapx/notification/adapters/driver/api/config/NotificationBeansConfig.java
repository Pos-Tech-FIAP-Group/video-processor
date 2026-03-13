package com.fiap.fiapx.notification.adapters.driver.api.config;

import com.fiap.fiapx.notification.adapters.driver.api.consumer.ProcessingEventConsumer;
import com.fiap.fiapx.notification.core.application.ports.GetUserByUuidPort;
import com.fiap.fiapx.notification.core.application.ports.SendNotificationPort;
import com.fiap.fiapx.notification.core.application.usecases.HandleProcessingCompletedUseCase;
import com.fiap.fiapx.notification.core.application.usecases.HandleProcessingFailedUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationBeansConfig {

    @Bean
    public HandleProcessingCompletedUseCase handleProcessingCompletedUseCase(
        SendNotificationPort sendNotificationPort
    ) {
        return new HandleProcessingCompletedUseCase(sendNotificationPort);
    }

    @Bean
    public HandleProcessingFailedUseCase handleProcessingFailedUseCase(
        GetUserByUuidPort getUserByUuidPort,
        SendNotificationPort sendNotificationPort
    ) {
        return new HandleProcessingFailedUseCase(getUserByUuidPort, sendNotificationPort);
    }

    @Bean
    public ProcessingEventConsumer processingEventConsumer(
        HandleProcessingCompletedUseCase handleProcessingCompletedUseCase,
        HandleProcessingFailedUseCase handleProcessingFailedUseCase
    ) {
        return new ProcessingEventConsumer(
            handleProcessingCompletedUseCase,
            handleProcessingFailedUseCase
        );
    }
}