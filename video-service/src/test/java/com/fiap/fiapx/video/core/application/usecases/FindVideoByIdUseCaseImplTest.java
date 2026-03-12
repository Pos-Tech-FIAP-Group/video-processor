package com.fiap.fiapx.video.core.application.usecases;

import com.fiap.fiapx.video.core.application.exceptions.VideoNotFoundException;
import com.fiap.fiapx.video.core.application.ports.VideoRepositoryPort;
import com.fiap.fiapx.video.core.domain.enums.VideoStatus;
import com.fiap.fiapx.video.core.domain.model.Video;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindVideoByIdUseCaseImplTest {

    @Mock
    private VideoRepositoryPort repository;

    @InjectMocks
    private FindVideoByIdUseCaseImpl useCase;

    @Test
    void deve_retornar_video_quando_existir() {
        UUID id = UUID.randomUUID();
        Video video = video(id);
        when(repository.findById(id)).thenReturn(Optional.of(video));

        Video result = useCase.execute(id);

        assertThat(result).isSameAs(video);
    }

    @Test
    void deve_lancar_excecao_quando_video_nao_existir() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(id))
                .isInstanceOf(VideoNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    private Video video(UUID id) {
        Instant now = Instant.now();
        return new Video(id, UUID.randomUUID(), "video.mp4", "video/mp4", "/tmp/video.mp4", null,
                VideoStatus.PROCESSANDO, null, null, now, now, null);
    }
}
