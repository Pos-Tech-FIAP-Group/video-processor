package com.fiap.fiapx.video.adapters.driver.api.files;

import com.fiap.fiapx.video.core.application.ports.DeleteTempVideoPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Component
public class TempFileStorage implements DeleteTempVideoPort {

    private final Path baseDir;

    public TempFileStorage(@Value("${app.storage.temp-dir}") String baseDir) {
        this.baseDir = Paths.get(baseDir).toAbsolutePath().normalize();
    }

    public String store(UUID userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo de vídeo é obrigatório.");
        }

        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("video");
        String safeName = original.replaceAll("[^a-zA-Z0-9._-]", "_");

        String fileName = UUID.randomUUID() + "_" + safeName;

        Path userDir = baseDir.resolve(userId.toString());
        Path target = userDir.resolve(fileName);

        try {
            Files.createDirectories(userDir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return target.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao salvar o vídeo em diretório temporário.", e);
        }
    }

    @Override
    public void deleteIfExists(String videoPath) {
        if (videoPath == null || videoPath.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(Paths.get(videoPath).toAbsolutePath().normalize());
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao remover o vídeo do diretório temporário.", e);
        }
    }
}