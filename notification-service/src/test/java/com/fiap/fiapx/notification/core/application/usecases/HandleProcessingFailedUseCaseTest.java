package com.fiap.fiapx.notification.core.application.usecases;

import com.fiap.fiapx.notification.core.application.ports.GetUserByUuidPort;
import com.fiap.fiapx.notification.core.application.ports.SendNotificationPort;
import com.fiap.fiapx.notification.core.domain.model.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandleProcessingFailedUseCaseTest {

    @Mock
    private GetUserByUuidPort getUserByUuidPort;

    @Mock
    private SendNotificationPort sendNotificationPort;

    private HandleProcessingFailedUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new HandleProcessingFailedUseCase(getUserByUuidPort, sendNotificationPort);
    }

    @Test
    void quando_userId_null_nao_chama_porta_ni_envio() {
        useCase.execute("video-123", null, "Erro de processamento");

        verify(getUserByUuidPort, never()).findByUserUuid(anyString());
        verify(sendNotificationPort, never()).sendProcessingFailedNotification(anyString(), anyString(), anyString());
    }

    @Test
    void quando_userId_em_branco_nao_chama_porta_ni_envio() {
        useCase.execute("video-123", "   ", "Erro de processamento");

        verify(getUserByUuidPort, never()).findByUserUuid(anyString());
        verify(sendNotificationPort, never()).sendProcessingFailedNotification(anyString(), anyString(), anyString());
    }

    @Test
    void quando_usuario_nao_encontrado_nao_envia_email() {
        when(getUserByUuidPort.findByUserUuid("user-uuid-456")).thenReturn(Optional.empty());

        useCase.execute("video-123", "user-uuid-456", "Falha no FFmpeg");

        verify(getUserByUuidPort).findByUserUuid("user-uuid-456");
        verify(sendNotificationPort, never()).sendProcessingFailedNotification(anyString(), anyString(), anyString());
    }

    @Test
    void quando_usuario_encontrado_envia_email_para_o_email_do_usuario() {
        UserInfo userInfo = new UserInfo("user@example.com", "john");
        when(getUserByUuidPort.findByUserUuid("user-uuid-789")).thenReturn(Optional.of(userInfo));

        useCase.execute("video-456", "user-uuid-789", "Timeout no processamento");

        ArgumentCaptor<String> videoIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sendNotificationPort).sendProcessingFailedNotification(
                videoIdCaptor.capture(), emailCaptor.capture(), messageCaptor.capture());

        assertThat(videoIdCaptor.getValue()).isEqualTo("video-456");
        assertThat(emailCaptor.getValue()).isEqualTo("user@example.com");
        assertThat(messageCaptor.getValue()).isEqualTo("Timeout no processamento");
    }

    @Test
    void quando_errorMessage_null_usa_mensagem_padrao() {
        UserInfo userInfo = new UserInfo("a@b.com", "user");
        when(getUserByUuidPort.findByUserUuid("uuid")).thenReturn(Optional.of(userInfo));

        useCase.execute("v1", "uuid", null);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sendNotificationPort).sendProcessingFailedNotification(anyString(), anyString(), messageCaptor.capture());
        assertThat(messageCaptor.getValue()).isEqualTo("Erro desconhecido");
    }
}
