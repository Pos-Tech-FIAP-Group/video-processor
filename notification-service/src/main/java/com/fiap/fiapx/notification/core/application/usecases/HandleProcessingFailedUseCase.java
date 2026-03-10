package com.fiap.fiapx.notification.core.application.usecases;

import com.fiap.fiapx.notification.core.application.ports.SendNotificationPort;

public class HandleProcessingFailedUseCase {

    private final SendNotificationPort sendNotificationPort;

    public HandleProcessingFailedUseCase(SendNotificationPort sendNotificationPort) {
        this.sendNotificationPort = sendNotificationPort;
    }

    public void execute(String videoId, String errorMessage) {
        String safeErrorMessage = (errorMessage == null || errorMessage.isBlank())
            ? "Erro desconhecido"
            : errorMessage;

        sendNotificationPort.sendProcessingFailedNotification(videoId, safeErrorMessage);
    }
}