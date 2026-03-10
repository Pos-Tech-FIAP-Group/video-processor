package com.fiap.fiapx.video.adapters.driven.infra.messaging.adapter;

import com.fiap.fiapx.video.adapters.driven.infra.messaging.messages.VideoProcessingCompletedMessage;
import com.fiap.fiapx.video.core.application.usecases.UpdateVideoProcessingResultUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VideoProcessingResultListenerTest {

    @Mock
    private UpdateVideoProcessingResultUseCase useCase;

    @InjectMocks
    private VideoProcessingResultListener listener;

    @Test
    void deve_processar_mensagem_de_sucesso() {
        UUID videoId = UUID.randomUUID();
        VideoProcessingCompletedMessage message = new VideoProcessingCompletedMessage(
                videoId,
                true,
                50,
                "/tmp/video.zip",
                null
        );

        listener.onMessage(message);

        verify(useCase).executeSuccess(videoId, 50, "/tmp/video.zip");
    }

    @Test
    void deve_processar_mensagem_de_falha_com_mensagem_informada() {
        UUID videoId = UUID.randomUUID();
        VideoProcessingCompletedMessage message = new VideoProcessingCompletedMessage(
                videoId,
                false,
                null,
                null,
                "falha no processamento"
        );

        listener.onMessage(message);

        verify(useCase).executeFailure(videoId, "falha no processamento");
    }

    @Test
    void deve_usar_mensagem_padrao_quando_falha_nao_informar_erro() {
        UUID videoId = UUID.randomUUID();
        VideoProcessingCompletedMessage message = new VideoProcessingCompletedMessage(
                videoId,
                false,
                null,
                null,
                null
        );

        listener.onMessage(message);

        verify(useCase).executeFailure(videoId, "Erro desconhecido no processamento");
    }
}
