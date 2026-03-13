package com.fiap.fiapx.notification.core.application.ports;

public interface SendNotificationPort {

    void sendProcessingCompletedNotification(String videoId, Integer frameCount, String zipPath);

    /**
     * Envia e-mail de falha para o destinatario.
     *
     * @param videoId      ID do video que falhou
     * @param toEmail      e-mail do destinatario (obtido do auth pelo userId no use case)
     * @param errorMessage mensagem de erro
     */
    void sendProcessingFailedNotification(String videoId, String toEmail, String errorMessage);
}