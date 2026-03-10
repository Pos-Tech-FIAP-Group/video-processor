package com.fiap.fiapx.notification.core.application.ports;

public interface SendNotificationPort {

    void sendProcessingCompletedNotification(String videoId, Integer frameCount, String zipPath);

    void sendProcessingFailedNotification(String videoId, String errorMessage);
}