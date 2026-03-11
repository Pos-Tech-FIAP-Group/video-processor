package com.fiap.fiapx.video.core.domain.model;

import com.fiap.fiapx.video.core.domain.enums.VideoStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VideoTest {

    @Test
    void deve_criar_video_com_status_processando() {
        Video video = Video.create(
                UUID.randomUUID(),
                "video.mp4",
                "video/mp4",
                "/tmp/video.mp4"
        );

        assertThat(video.getId()).isNotNull();
        assertThat(video.getStatus()).isEqualTo(VideoStatus.PROCESSANDO);
        assertThat(video.getZipPath()).isNull();
        assertThat(video.getFrameCount()).isNull();
        assertThat(video.getErrorMessage()).isNull();
        assertThat(video.getProcessedAt()).isNull();
        assertThat(video.getCreatedAt()).isNotNull();
        assertThat(video.getUpdatedAt()).isNotNull();
    }

    @Test
    void deve_finalizar_processamento_quando_status_for_processando() {
        Video video = novoVideo(VideoStatus.PROCESSANDO);

        Video processed = video.completeProcessing(42, "/tmp/video.zip");

        assertThat(processed.getStatus()).isEqualTo(VideoStatus.CONCLUIDO);
        assertThat(processed.getFrameCount()).isEqualTo(42);
        assertThat(processed.getZipPath()).isEqualTo("/tmp/video.zip");
        assertThat(processed.getErrorMessage()).isNull();
        assertThat(processed.getProcessedAt()).isNotNull();
        assertThat(processed.getUpdatedAt()).isNotNull();
        assertThat(processed.getId()).isEqualTo(video.getId());
        assertThat(processed.getCreatedAt()).isEqualTo(video.getCreatedAt());
    }

    @Test
    void nao_deve_finalizar_processamento_quando_status_for_invalido() {
        Video video = novoVideo(VideoStatus.PENDENTE);

        assertThatThrownBy(() -> video.completeProcessing(42, "/tmp/video.zip"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Não é possível finalizar o processamento");
    }

    @Test
    void nao_deve_finalizar_processamento_quando_frame_count_for_invalido() {
        Video video = novoVideo(VideoStatus.PROCESSANDO);

        assertThatThrownBy(() -> video.completeProcessing(-1, "/tmp/video.zip"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("frameCount inválido");
    }

    @Test
    void nao_deve_finalizar_processamento_quando_zip_path_for_invalido() {
        Video video = novoVideo(VideoStatus.PROCESSANDO);

        assertThatThrownBy(() -> video.completeProcessing(10, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("zipPath é obrigatório");
    }

    @Test
    void deve_marcar_erro_quando_status_for_processando() {
        Video video = novoVideo(VideoStatus.PROCESSANDO);

        Video failed = video.failProcessing("falha no ffmpeg");

        assertThat(failed.getStatus()).isEqualTo(VideoStatus.ERRO);
        assertThat(failed.getErrorMessage()).isEqualTo("falha no ffmpeg");
        assertThat(failed.getProcessedAt()).isNotNull();
        assertThat(failed.getUpdatedAt()).isNotNull();
        assertThat(failed.getFrameCount()).isEqualTo(video.getFrameCount());
        assertThat(failed.getZipPath()).isEqualTo(video.getZipPath());
    }

    @Test
    void nao_deve_marcar_erro_quando_status_for_invalido() {
        Video video = novoVideo(VideoStatus.CONCLUIDO);

        assertThatThrownBy(() -> video.failProcessing("erro"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Não é possível marcar erro");
    }

    @Test
    void nao_deve_marcar_erro_quando_mensagem_for_vazia() {
        Video video = novoVideo(VideoStatus.PROCESSANDO);

        assertThatThrownBy(() -> video.failProcessing("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("mensagem de erro é obrigatória");
    }

    @Test
    void deve_validar_campos_obrigatorios_no_construtor() {
        Instant now = Instant.now();
        UUID userId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();

        assertThatThrownBy(() -> new Video(
                null,
                userId,
                "video.mp4",
                "video/mp4",
                "/tmp/video.mp4",
                null,
                VideoStatus.PROCESSANDO,
                null,
                null,
                now,
                now,
                null
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Id é obrigatório");

        assertThatThrownBy(() -> new Video(
                videoId,
                userId,
                " ",
                "video/mp4",
                "/tmp/video.mp4",
                null,
                VideoStatus.PROCESSANDO,
                null,
                null,
                now,
                now,
                null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Nome original do arquivo é obrigatório");
    }

    private Video novoVideo(VideoStatus status) {
        Instant createdAt = Instant.parse("2026-03-01T10:15:30Z");
        Instant updatedAt = Instant.parse("2026-03-01T10:15:30Z");

        return new Video(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "video.mp4",
                "video/mp4",
                "/tmp/video.mp4",
                null,
                status,
                5,
                null,
                createdAt,
                updatedAt,
                null
        );
    }
}
