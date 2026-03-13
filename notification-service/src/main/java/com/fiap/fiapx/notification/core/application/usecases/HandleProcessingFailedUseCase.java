package com.fiap.fiapx.notification.core.application.usecases;

import com.fiap.fiapx.notification.core.application.ports.GetUserByUuidPort;
import com.fiap.fiapx.notification.core.application.ports.SendNotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandleProcessingFailedUseCase {

    private static final Logger logger = LoggerFactory.getLogger(HandleProcessingFailedUseCase.class);

    private final GetUserByUuidPort getUserByUuidPort;
    private final SendNotificationPort sendNotificationPort;

    public HandleProcessingFailedUseCase(
            GetUserByUuidPort getUserByUuidPort,
            SendNotificationPort sendNotificationPort) {
        this.getUserByUuidPort = getUserByUuidPort;
        this.sendNotificationPort = sendNotificationPort;
    }

    /**
     * Em evento de falha: se houver userId, obtem o e-mail do auth e envia notificacao.
     * Se userId for nulo ou em branco, ou usuario nao encontrado, apenas loga e nao envia.
     */
    public void execute(String videoId, String userId, String errorMessage) {
        String safeErrorMessage = (errorMessage == null || errorMessage.isBlank())
                ? "Erro desconhecido"
                : errorMessage;

        if (userId == null || userId.isBlank()) {
            logger.info("Evento de falha sem userId: nao enviando e-mail. videoId={}", videoId);
            return;
        }

        getUserByUuidPort.findByUserUuid(userId)
                .ifPresentOrElse(
                        userInfo -> sendNotificationPort.sendProcessingFailedNotification(
                                videoId, userInfo.email(), safeErrorMessage),
                        () -> logger.warn("Usuario nao encontrado para uuid={}: nao enviando e-mail. videoId={}",
                                userId, videoId)
                );
    }
}
