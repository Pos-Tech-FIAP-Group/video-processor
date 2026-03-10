package com.fiap.fiapx.notification.adapters.driven.infra.mail;

import com.fiap.fiapx.notification.core.application.ports.SendNotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationAdapter implements SendNotificationPort {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationAdapter.class);

    @Override
    public void sendProcessingCompletedNotification(String videoId, Integer frameCount, String zipPath) {
        logger.info("""
                [NOTIFICATION] PROCESSING COMPLETED
                videoId: {}
                frameCount: {}
                zipPath: {}
                """,
            videoId,
            frameCount,
            zipPath
        );
    }

    @Override
    public void sendProcessingFailedNotification(String videoId, String errorMessage) {
        logger.error("""
                [NOTIFICATION] PROCESSING FAILED
                videoId: {}
                errorMessage: {}
                """,
            videoId,
            errorMessage
        );
    }
}