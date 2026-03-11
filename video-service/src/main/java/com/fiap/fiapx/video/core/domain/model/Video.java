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

    @SuppressWarnings("java:S107")
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
        this.status = Objects.requireNonNull(status, "Status é obrigatório");
        this.createdAt = Objects.requireNonNull(createdAt, "Data de criação é obrigatória");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Data de atualização é obrigatória");

        this.zipPath = zipPath;
        this.frameCount = frameCount;
        this.errorMessage = errorMessage;
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
                VideoStatus.PROCESSANDO,
                null,
                null,
                now,
                now,
                null
        );
    }


    public Video completeProcessing(Integer frameCount, String zipPath) {
        if (this.status != VideoStatus.PROCESSANDO) {
            throw new IllegalStateException(
                    "Não é possível finalizar o processamento a partir do status " + this.status
            );
        }

        if (frameCount == null || frameCount < 0) {
            throw new IllegalArgumentException("frameCount inválido");
        }

        if (zipPath == null || zipPath.isBlank()) {
            throw new IllegalArgumentException("zipPath é obrigatório");
        }

        Instant now = Instant.now();

        return new Video(
                this.id,
                this.userId,
                this.originalFilename,
                this.contentType,
                this.videoPath,
                zipPath,
                VideoStatus.CONCLUIDO,
                frameCount,
                null,
                this.createdAt,
                now,
                now
        );
    }

    public Video failProcessing(String errorMessage) {
        if (this.status != VideoStatus.PROCESSANDO) {
            throw new IllegalStateException(
                    "Não é possível marcar erro a partir do status " + this.status
            );
        }

        if (errorMessage == null || errorMessage.isBlank()) {
            throw new IllegalArgumentException("mensagem de erro é obrigatória");
        }

        Instant now = Instant.now();

        return new Video(
                this.id,
                this.userId,
                this.originalFilename,
                this.contentType,
                this.videoPath,
                this.zipPath,
                VideoStatus.ERRO,
                this.frameCount,
                errorMessage,
                this.createdAt,
                now,
                now
        );
    }

    private static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

}