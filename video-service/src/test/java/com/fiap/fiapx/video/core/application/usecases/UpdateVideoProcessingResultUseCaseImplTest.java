package com.fiap.fiapx.video.core.application.usecases;

import com.fiap.fiapx.video.core.application.exceptions.VideoNotFoundException;
import com.fiap.fiapx.video.core.application.ports.DeleteTempVideoPort;
import com.fiap.fiapx.video.core.application.ports.VideoRepositoryPort;
import com.fiap.fiapx.video.core.domain.enums.VideoStatus;
import com.fiap.fiapx.video.core.domain.model.Video;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateVideoProcessingResultUseCaseImplTest {

    @Mock
    private VideoRepositoryPort repository;

    @Mock
    private DeleteTempVideoPort deleteTempVideoPort;

    @InjectMocks
    private UpdateVideoProcessingResultUseCaseImpl useCase;

    @Test
    void deve_atualizar_video_para_concluido() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(videoProcessando(id)));

        useCase.executeSuccess(id, 30, "/tmp/video.zip");

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(VideoStatus.CONCLUIDO);
        assertThat(captor.getValue().getFrameCount()).isEqualTo(30);
        assertThat(captor.getValue().getZipPath()).isEqualTo("/tmp/video.zip");
    }

    @Test
    void deve_atualizar_video_para_erro() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(videoProcessando(id)));

        useCase.executeFailure(id, "falha ao extrair frames");

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(VideoStatus.ERRO);
        assertThat(captor.getValue().getErrorMessage()).isEqualTo("falha ao extrair frames");
    }

    @Test
    void deve_lancar_excecao_quando_video_nao_existir_no_fluxo_de_sucesso() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executeSuccess(id, 10, "/tmp/video.zip"))
                .isInstanceOf(VideoNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void deve_lancar_excecao_quando_video_nao_existir_no_fluxo_de_falha() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executeFailure(id, "erro"))
                .isInstanceOf(VideoNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void deve_atualizar_video_e_excluir_arquivo_temporario_quando_processamento_for_concluido() {
        UUID id = UUID.randomUUID();
        Video video = videoProcessando(id);

        when(repository.findById(id)).thenReturn(Optional.of(video));

        useCase.executeSuccess(id, 30, "/tmp/video.zip");

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(repository).save(captor.capture());

        Video videoSalvo = captor.getValue();

        assertThat(videoSalvo.getStatus()).isEqualTo(VideoStatus.CONCLUIDO);
        assertThat(videoSalvo.getFrameCount()).isEqualTo(30);
        assertThat(videoSalvo.getZipPath()).isEqualTo("/tmp/video.zip");

        verify(deleteTempVideoPort).deleteIfExists(videoSalvo.getVideoPath());
    }

    @Test
    void nao_deve_excluir_arquivo_temporario_quando_processamento_falhar() {
        UUID id = UUID.randomUUID();
        Video video = videoProcessando(id);

        when(repository.findById(id)).thenReturn(Optional.of(video));

        useCase.executeFailure(id, "erro no processamento");

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(repository).save(captor.capture());

        Video videoSalvo = captor.getValue();

        assertThat(videoSalvo.getStatus()).isEqualTo(VideoStatus.ERRO);
        assertThat(videoSalvo.getErrorMessage()).isEqualTo("erro no processamento");

        verify(deleteTempVideoPort, org.mockito.Mockito.never())
                .deleteIfExists(org.mockito.ArgumentMatchers.any());
    }

    private Video videoProcessando(UUID id) {
        Instant now = Instant.now();
        return new Video(id, UUID.randomUUID(), "video.mp4", "video/mp4", "/tmp/videos/123/videoteste.mp4", null,
                VideoStatus.PROCESSANDO, null, null, now, now, null);
    }
}
