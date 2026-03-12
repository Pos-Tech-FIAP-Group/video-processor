package com.fiap.fiapx.video.adapters.driver.api.files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TempFileStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void deve_salvar_arquivo_em_diretorio_do_usuario() throws IOException {
        TempFileStorage storage = new TempFileStorage(tempDir.toString());
        UUID userId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video teste.mp4",
                "video/mp4",
                "conteudo".getBytes()
        );

        String storedPath = storage.store(userId, file);

        Path path = Path.of(storedPath);
        assertThat(path).exists();
        assertThat(path.getParent().getFileName()).hasToString(userId.toString());
        assertThat(path.getFileName().toString()).contains("video_teste.mp4");
        assertThat(Files.readString(path)).isEqualTo("conteudo");
    }

    @Test
    void deve_lancar_excecao_quando_arquivo_for_nulo_ou_vazio() {
        TempFileStorage storage = new TempFileStorage(tempDir.toString());

        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> storage.store(userId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Arquivo de vídeo é obrigatório.");

        MockMultipartFile empty = new MockMultipartFile("file", new byte[0]);

        assertThatThrownBy(() -> storage.store(userId, empty))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Arquivo de vídeo é obrigatório.");
    }

    @Test
    void deve_lancar_excecao_quando_ocorrer_erro_de_io() {
        TempFileStorage storage = new TempFileStorage(tempDir.toString());
        UUID userId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "x".getBytes()) {
            @Override
            public InputStream getInputStream() throws IOException {
                throw new IOException("erro de IO");
            }
        };

        assertThatThrownBy(() -> storage.store(userId, file))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Falha ao salvar o vídeo em diretório temporário.")
                .hasCauseInstanceOf(IOException.class);
    }
}
