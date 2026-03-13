package com.fiap.fiapx.notification.adapters.driven.infra.mail.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotificationAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailNotificationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new EmailNotificationAdapter(mailSender);
    }

    @Test
    void sendProcessingFailedNotification_com_email_chama_send() {
        adapter.sendProcessingFailedNotification("v2", "user@example.com", "Erro de teste");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertThat(msg.getTo()).containsExactly("user@example.com");
        assertThat(msg.getSubject()).isEqualTo("Falha no processamento do vídeo");
        assertThat(msg.getText()).contains("v2", "Erro de teste");
    }

    @Test
    void sendProcessingFailedNotification_com_toEmail_vazio_nao_chama_send() {
        adapter.sendProcessingFailedNotification("v3", null, "Erro");
        adapter.sendProcessingFailedNotification("v3", "   ", "Erro");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
