package com.fiap.fiapx.video.adapters.driven.infra.persistence.mapper;

import com.fiap.fiapx.video.adapters.driven.infra.persistence.entity.VideoEntity;
import com.fiap.fiapx.video.core.domain.enums.VideoStatus;
import com.fiap.fiapx.video.core.domain.model.Video;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VideoMapperTest {

    @Test
    void deve_mapear_domain_para_entity() {
        Instant now = Instant.parse("2026-03-01T10:15:30Z");
        Video video = new Video(
                UUID.randomUUID(), UUID.randomUUID(), "video.mp4", "video/mp4", "/tmp/video.mp4",
                "/tmp/video.zip", VideoStatus.CONCLUIDO, 20, null, now, now, now
        );

        VideoEntity entity = VideoMapper.toEntity(video);

        assertThat(entity.getId()).isEqualTo(video.getId());
        assertThat(entity.getUserId()).isEqualTo(video.getUserId());
        assertThat(entity.getOriginalFilename()).isEqualTo("video.mp4");
        assertThat(entity.getContentType()).isEqualTo("video/mp4");
        assertThat(entity.getVideoPath()).isEqualTo("/tmp/video.mp4");
        assertThat(entity.getZipPath()).isEqualTo("/tmp/video.zip");
        assertThat(entity.getStatus()).isEqualTo(VideoStatus.CONCLUIDO);
        assertThat(entity.getFrameCount()).isEqualTo(20);
        assertThat(entity.getProcessedAt()).isEqualTo(now);
    }

    @Test
    void deve_mapear_entity_para_domain() {
        Instant now = Instant.parse("2026-03-01T10:15:30Z");
        VideoEntity entity = new VideoEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(UUID.randomUUID());
        entity.setOriginalFilename("video.mp4");
        entity.setContentType("video/mp4");
        entity.setVideoPath("/tmp/video.mp4");
        entity.setZipPath(null);
        entity.setStatus(VideoStatus.PROCESSANDO);
        entity.setFrameCount(null);
        entity.setErrorMessage(null);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setProcessedAt(null);

        Video video = VideoMapper.toDomain(entity);

        assertThat(video.getId()).isEqualTo(entity.getId());
        assertThat(video.getUserId()).isEqualTo(entity.getUserId());
        assertThat(video.getOriginalFilename()).isEqualTo(entity.getOriginalFilename());
        assertThat(video.getContentType()).isEqualTo(entity.getContentType());
        assertThat(video.getVideoPath()).isEqualTo(entity.getVideoPath());
        assertThat(video.getStatus()).isEqualTo(VideoStatus.PROCESSANDO);
        assertThat(video.getCreatedAt()).isEqualTo(now);
    }
}
