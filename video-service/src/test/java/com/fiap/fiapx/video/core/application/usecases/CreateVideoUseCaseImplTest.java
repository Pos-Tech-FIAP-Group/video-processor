package com.fiap.fiapx.video.core.application.usecases;

import com.fiap.fiapx.video.core.application.ports.PublishVideoProcessingRequestedPort;
import com.fiap.fiapx.video.core.application.ports.VideoRepositoryPort;
import com.fiap.fiapx.video.core.application.usecases.command.CreateVideoCommand;
import com.fiap.fiapx.video.core.domain.enums.VideoStatus;
import com.fiap.fiapx.video.core.domain.model.Video;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateVideoUseCaseImplTest {

    @Mock
    private VideoRepositoryPort repository;

    @Mock
    private PublishVideoProcessingRequestedPort publishPort;

    @InjectMocks
    private CreateVideoUseCaseImpl useCase;

    @Test
    void deve_salvar_video_e_publicar_evento_com_intervalo_informado() {
        UUID userId = UUID.randomUUID();
        CreateVideoCommand command = new CreateVideoCommand(
                userId,
                "video.mp4",
                "video/mp4",
                "/tmp/video.mp4",
                2.5
        );

        Video result = useCase.execute(command);

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(repository).save(captor.capture());
        verify(publishPort).publish(
                eq(result.getId()),
                eq(userId),
                eq("/tmp/video.mp4"),
                eq(2.5)
        );

        Video saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(VideoStatus.PROCESSANDO);
        assertThat(saved.getOriginalFilename()).isEqualTo("video.mp4");
        assertThat(result.getId()).isEqualTo(saved.getId());
    }

    @Test
    void deve_usar_intervalo_padrao_quando_nao_for_informado() {
        UUID userId = UUID.randomUUID();
        CreateVideoCommand command = new CreateVideoCommand(
                userId,
                "video.mp4",
                "video/mp4",
                "/tmp/video.mp4",
                null
        );

        Video result = useCase.execute(command);

        verify(publishPort).publish(result.getId(), userId, "/tmp/video.mp4", 1.0);
    }

    @Test
    void deve_usar_intervalo_padrao_quando_valor_for_menor_ou_igual_a_zero() {
        UUID userId = UUID.randomUUID();
        CreateVideoCommand command = new CreateVideoCommand(
                userId,
                "video.mp4",
                "video/mp4",
                "/tmp/video.mp4",
                0.0
        );

        Video result = useCase.execute(command);

        verify(publishPort).publish(result.getId(), userId, "/tmp/video.mp4", 1.0);
    }

    @Test
    void deve_marcar_video_como_erro_e_relancar_excecao_quando_publicacao_falhar() {
        UUID userId = UUID.randomUUID();
        CreateVideoCommand command = new CreateVideoCommand(
                userId,
                "video.mp4",
                "video/mp4",
                "/tmp/video.mp4",
                1.0
        );

        RuntimeException exception = new RuntimeException("rabbit indisponível");
        doThrow(exception).when(publishPort).publish(any(), eq(userId), eq("/tmp/video.mp4"), eq(1.0));

        assertThatThrownBy(() -> useCase.execute(command))
                .isSameAs(exception);

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(repository, times(2)).save(captor.capture());

        assertThat(captor.getAllValues().get(0).getStatus()).isEqualTo(VideoStatus.PROCESSANDO);
        assertThat(captor.getAllValues().get(1).getStatus()).isEqualTo(VideoStatus.ERRO);
        assertThat(captor.getAllValues().get(1).getErrorMessage())
                .contains("Falha ao solicitar processamento: rabbit indisponível");
    }
}
