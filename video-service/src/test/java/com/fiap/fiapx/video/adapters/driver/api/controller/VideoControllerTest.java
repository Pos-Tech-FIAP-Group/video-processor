package com.fiap.fiapx.video.adapters.driver.api.controller;

import com.fiap.fiapx.video.adapters.driver.api.exceptionhandler.RestExceptionHandler;
import com.fiap.fiapx.video.adapters.driver.api.files.TempFileStorage;
import com.fiap.fiapx.video.adapters.driver.api.security.JwtAuthenticationFilter;
import com.fiap.fiapx.video.core.application.exceptions.VideoNotFoundException;
import com.fiap.fiapx.video.core.application.usecases.CreateVideoUseCase;
import com.fiap.fiapx.video.core.application.usecases.FindVideoByIdUseCase;
import com.fiap.fiapx.video.core.application.usecases.ListVideosByUserUseCase;
import com.fiap.fiapx.video.core.domain.enums.VideoStatus;
import com.fiap.fiapx.video.core.domain.model.Video;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VideoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RestExceptionHandler.class)
class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateVideoUseCase createVideoUseCase;

    @MockitoBean
    private FindVideoByIdUseCase findVideoByIdUseCase;

    @MockitoBean
    private ListVideosByUserUseCase listVideosByUserUseCase;

    @MockitoBean
    private TempFileStorage tempFileStorage;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void deve_retornar_202_quando_upload_for_valido() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        Instant now = Instant.now();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "conteudo".getBytes()
        );

        Video video = new Video(
                videoId,
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
        );

        given(tempFileStorage.store(eq(userId), any())).willReturn("/tmp/video.mp4");
        given(createVideoUseCase.execute(any())).willReturn(video);

        mockMvc.perform(
                        multipart("/api/videos")
                                .file(file)
                                .header("X-User-Id", userId.toString())
                                .param("frameIntervalSeconds", "2.5")
                )
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(videoId.toString()))
                .andExpect(jsonPath("$.status").value("PROCESSANDO"));

        verify(tempFileStorage).store(eq(userId), any());

        ArgumentCaptor<com.fiap.fiapx.video.core.application.usecases.command.CreateVideoCommand> captor =
                ArgumentCaptor.forClass(com.fiap.fiapx.video.core.application.usecases.command.CreateVideoCommand.class);

        verify(createVideoUseCase).execute(captor.capture());

        var command = captor.getValue();
        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.originalFilename()).isEqualTo("video.mp4");
        assertThat(command.contentType()).isEqualTo("video/mp4");
        assertThat(command.videoPath()).isEqualTo("/tmp/video.mp4");
        assertThat(command.frameIntervalSeconds()).isEqualTo(2.5);
    }

    @Test
    void deve_retornar_400_quando_header_user_id_estiver_ausente_no_create() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "conteudo".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/videos")
                                .file(file)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deve_retornar_400_quando_header_user_id_for_invalido_no_create() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "conteudo".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/videos")
                                .file(file)
                                .header("X-User-Id", "uuid-invalido")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deve_retornar_200_quando_busca_por_id_for_valida() throws Exception {
        UUID videoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        Video video = new Video(
                videoId,
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
        );

        given(findVideoByIdUseCase.execute(videoId)).willReturn(video);

        mockMvc.perform(get("/api/videos/{id}", videoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(videoId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.status").value("PROCESSANDO"));
    }

    @Test
    void deve_retornar_400_quando_id_for_invalido_no_find_by_id() throws Exception {
        mockMvc.perform(get("/api/videos/{id}", "id-invalido"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deve_retornar_404_quando_video_nao_existir_no_find_by_id() throws Exception {
        UUID videoId = UUID.randomUUID();

        given(findVideoByIdUseCase.execute(videoId))
                .willThrow(new VideoNotFoundException(videoId));

        mockMvc.perform(get("/api/videos/{id}", videoId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deve_retornar_200_quando_listar_videos_por_usuario() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        Instant now = Instant.now();

        Video video = new Video(
                videoId,
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
        );

        var pageResult = new com.fiap.fiapx.video.core.application.common.PageResult<>(
                List.of(video),
                0,
                10,
                1,
                1
        );

        given(listVideosByUserUseCase.execute(userId, 0, 10)).willReturn(pageResult);

        mockMvc.perform(get("/api/videos")
                        .param("userId", userId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.items[0].id").value(videoId.toString()))
                .andExpect(jsonPath("$.items[0].status").value("PROCESSANDO"));
    }

    @Test
    void deve_retornar_400_quando_user_id_for_invalido_na_listagem() throws Exception {
        mockMvc.perform(get("/api/videos")
                        .param("userId", "user-invalido")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }
}