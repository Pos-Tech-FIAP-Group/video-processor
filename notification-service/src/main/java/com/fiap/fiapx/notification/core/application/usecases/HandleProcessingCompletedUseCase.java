package com.fiap.fiapx.notification.core.application.usecases;

import com.fiap.fiapx.notification.core.application.ports.SendNotificationPort;

public class HandleProcessingCompletedUseCase {

    private final SendNotificationPort sendNotificationPort;

    public HandleProcessingCompletedUseCase(SendNotificationPort sendNotificationPort) {
        this.sendNotificationPort = sendNotificationPort;
    }

    public void execute(String videoId, Integer frameCount, String zipPath) {
        sendNotificationPort.sendProcessingCompletedNotification(videoId, frameCount, zipPath);
    }
}