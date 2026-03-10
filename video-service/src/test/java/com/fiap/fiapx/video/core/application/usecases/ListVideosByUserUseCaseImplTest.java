package com.fiap.fiapx.video.core.application.usecases;

import com.fiap.fiapx.video.core.application.common.PageResult;
import com.fiap.fiapx.video.core.application.ports.VideoRepositoryPort;
import com.fiap.fiapx.video.core.domain.model.Video;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListVideosByUserUseCaseImplTest {

    @Mock
    private VideoRepositoryPort repository;

    @InjectMocks
    private ListVideosByUserUseCaseImpl useCase;

    @Test
    void deve_delegar_busca_paginada_para_repositorio() {
        UUID userId = UUID.randomUUID();
        PageResult<Video> expected = new PageResult<>(List.of(), 1, 10, 0, 0);
        when(repository.findByUserId(userId, 1, 10)).thenReturn(expected);

        PageResult<Video> result = useCase.execute(userId, 1, 10);

        assertThat(result).isSameAs(expected);
        verify(repository).findByUserId(userId, 1, 10);
    }
}
