package com.fiap.fiapx.video.infra.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class VideoGetByIdIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deve_retornar_200_quando_video_existir() {
        UUID videoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        var now = java.sql.Timestamp.from(Instant.now());

        jdbcTemplate.update("""
            insert into videos (
                id, user_id, original_filename, content_type, video_path, zip_path,
                status, frame_count, error_message,
                created_at, updated_at, processed_at
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
                videoId,
                userId,
                "video.mp4",
                "video/mp4",
                "/tmp/video.mp4",
                null,
                "PENDENTE",
                null,
                null,
                now,
                now,
                null
        );

        ResponseEntity<Map> response =
                restTemplate.getForEntity("/api/videos/{id}", Map.class, videoId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("id")).isEqualTo(videoId.toString());
        assertThat(response.getBody().get("userId")).isEqualTo(userId.toString());
        assertThat(response.getBody().get("status")).isEqualTo("PENDENTE");
    }

    @Test
    void deve_retornar_404_quando_video_nao_existir() {
        UUID inexistente = UUID.randomUUID();

        ResponseEntity<Map> response =
                restTemplate.getForEntity("/api/videos/{id}", Map.class, inexistente);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("code")).isEqualTo("NOT_FOUND");
        assertThat(String.valueOf(response.getBody().get("message")))
                .contains(inexistente.toString());
        assertThat(response.getBody()).containsKey("timestamp");
    }

    @Test
    void deve_retornar_400_quando_uuid_for_invalido() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/videos/{id}", String.class, "id-invalido");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}