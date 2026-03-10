package com.fiap.fiapx.video.adapters.driver.api.mapper;

import com.fiap.fiapx.video.adapters.driver.api.dto.response.CreateVideoResponse;
import com.fiap.fiapx.video.adapters.driver.api.dto.response.VideoResponse;
import com.fiap.fiapx.video.core.domain.enums.VideoStatus;
import com.fiap.fiapx.video.core.domain.model.Video;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VideoApiMapperTest {

    @Test
    void deve_mapear_para_create_response() {
        Video video = video();

        CreateVideoResponse response = VideoApiMapper.toCreateResponse(video);

        assertThat(response.id()).isEqualTo(video.getId());
        assertThat(response.status()).isEqualTo(video.getStatus());
        assertThat(response.createdAt()).isEqualTo(video.getCreatedAt());
    }

    @Test
    void deve_mapear_para_video_response() {
        Video video = video();

        VideoResponse response = VideoApiMapper.toResponse(video);

        assertThat(response.id()).isEqualTo(video.getId());
        assertThat(response.userId()).isEqualTo(video.getUserId());
        assertThat(response.originalFilename()).isEqualTo(video.getOriginalFilename());
        assertThat(response.contentType()).isEqualTo(video.getContentType());
        assertThat(response.videoPath()).isEqualTo(video.getVideoPath());
        assertThat(response.zipPath()).isEqualTo(video.getZipPath());
        assertThat(response.status()).isEqualTo(video.getStatus());
        assertThat(response.frameCount()).isEqualTo(video.getFrameCount());
        assertThat(response.errorMessage()).isEqualTo(video.getErrorMessage());
        assertThat(response.createdAt()).isEqualTo(video.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(video.getUpdatedAt());
        assertThat(response.processedAt()).isEqualTo(video.getProcessedAt());
    }

    private Video video() {
        Instant now = Instant.parse("2026-03-01T10:15:30Z");
        return new Video(
                UUID.randomUUID(), UUID.randomUUID(), "video.mp4", "video/mp4", "/tmp/video.mp4",
                "/tmp/video.zip", VideoStatus.CONCLUIDO, 20, null, now, now, now
        );
    }
}
