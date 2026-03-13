package com.fiap.fiapx.notification.adapters.driven.infra.mail.adapter;

import com.fiap.fiapx.notification.core.application.ports.SendNotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationAdapter implements SendNotificationPort {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationAdapter.class);

    private final JavaMailSender mailSender;

    public EmailNotificationAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendProcessingFailedNotification(String videoId, String toEmail, String errorMessage) {
        if (toEmail == null || toEmail.isBlank()) {
            logger.warn("Nao enviando e-mail de falha: destinatario vazio. videoId={}", videoId);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@fiapx.com");
            message.setTo(toEmail);
            message.setSubject("Falha no processamento do vídeo");
            message.setText("""
                    Ocorreu uma falha no processamento do vídeo.

                    videoId: %s
                    errorMessage: %s
                    """.formatted(videoId, errorMessage));

            mailSender.send(message);
            logger.info("E-mail de falha enviado. videoId={}, to={}", videoId, toEmail);
        } catch (Exception e) {
            logger.error("Falha ao enviar e-mail de erro. videoId={}", videoId, e);
        }
    }
}