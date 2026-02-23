package com.fiap.fiapx.video.adapters.driven.infra.persistence.entity;

import com.fiap.fiapx.video.core.domain.enums.VideoStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "videos")
@Getter
@Setter
@NoArgsConstructor
public class VideoEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "video_path", nullable = false)
    private String videoPath;

    @Column(name = "zip_path")
    private String zipPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoStatus status;

    @Column(name = "frame_count")
    private Integer frameCount;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "processed_at")
    private Instant processedAt;
}