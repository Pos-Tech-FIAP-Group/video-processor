package com.fiap.fiapx.video.core.domain.model;

import com.fiap.fiapx.video.core.domain.enums.VideoStatus;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Video {

    private final UUID id;
    private final UUID userId;

    private final String originalFilename;
    private final String contentType;

    private final String videoPath;
    private final String zipPath;

    private final VideoStatus status;

    private final Integer frameCount;
    private final String errorMessage;

    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant processedAt;

    public Video(
            UUID id,
            UUID userId,
            String originalFilename,
            String contentType,
            String videoPath,
            String zipPath,
            VideoStatus status,
            Integer frameCount,
            String errorMessage,
            Instant createdAt,
            Instant updatedAt,
            Instant processedAt
    ) {
        this.id = Objects.requireNonNull(id, "Id é obrigatório");
        this.userId = Objects.requireNonNull(userId, "UserId é obrigatório");

        this.originalFilename = requireNonBlank(originalFilename, "Nome original do arquivo é obrigatório");
        this.contentType = requireNonBlank(contentType, "Content-Type é obrigatório");

        this.videoPath = requireNonBlank(videoPath, "Caminho do vídeo é obrigatório");
        this.zipPath = zipPath;

        this.status = Objects.requireNonNull(status, "Status é obrigatório");

        this.frameCount = frameCount;
        this.errorMessage = errorMessage;

        this.createdAt = Objects.requireNonNull(createdAt, "Data de criação é obrigatória");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Data de atualização é obrigatória");
        this.processedAt = processedAt;
    }

        public static Video create(
            UUID userId,
            String originalFilename,
            String contentType,
            String videoPath
    ) {

        Instant now = Instant.now();

        return new Video(
                UUID.randomUUID(),
                userId,
                originalFilename,
                contentType,
                videoPath,
                null,
                VideoStatus.PENDENTE,
                null,
                null,
                now,
                now,
                null
        );
    }

    private static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

}