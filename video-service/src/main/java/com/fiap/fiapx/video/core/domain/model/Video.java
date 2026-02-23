package com.fiap.fiapx.video.core.domain.model;

import com.fiap.fiapx.video.core.domain.enums.VideoStatus;

import java.time.Instant;
import java.util.UUID;

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
        this.id = id;
        this.userId = userId;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.videoPath = videoPath;
        this.zipPath = zipPath;
        this.status = status;
        this.frameCount = frameCount;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.processedAt = processedAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getOriginalFilename() { return originalFilename; }
    public String getContentType() { return contentType; }
    public String getVideoPath() { return videoPath; }
    public String getZipPath() { return zipPath; }
    public VideoStatus getStatus() { return status; }
    public Integer getFrameCount() { return frameCount; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getProcessedAt() { return processedAt; }
}