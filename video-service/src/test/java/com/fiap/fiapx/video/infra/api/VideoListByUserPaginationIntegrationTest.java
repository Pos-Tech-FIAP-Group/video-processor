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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class VideoListByUserPaginationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deve_listar_videos_paginados_por_usuario_ordenados_por_createdAt_desc() {
        UUID userId = UUID.randomUUID();

        for (int i = 1; i <= 15; i++) {
            UUID videoId = UUID.randomUUID();
            Timestamp createdAt = Timestamp.from(Instant.parse("2026-02-25T00:00:00Z").plusSeconds(i));

            jdbcTemplate.update("""
                    insert into videos (
                        id, user_id, original_filename, content_type, video_path, zip_path,
                        status, frame_count, error_message,
                        created_at, updated_at, processed_at
                    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    videoId,
                    userId,
                    "video-" + i + ".mp4",
                    "video/mp4",
                    "/tmp/video-" + i + ".mp4",
                    null,
                    "PENDENTE",
                    null,
                    null,
                    createdAt,
                    createdAt,
                    null
            );
        }

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/videos?userId={userId}&page={page}&size={size}",
                Map.class,
                userId,
                0,
                10
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody().get("page")).isEqualTo(0);
        assertThat(response.getBody().get("size")).isEqualTo(10);
        assertThat(response.getBody().get("totalItems")).isEqualTo(15);
        assertThat(response.getBody().get("totalPages")).isEqualTo(2);

        Object itemsObj = response.getBody().get("items");
        assertThat(itemsObj).isInstanceOf(List.class);

        List<?> items = (List<?>) itemsObj;
        assertThat(items).hasSize(10);

        // Primeiro item deve ser o mais recente (video-15.mp4) por created_at desc
        Map<?, ?> first = (Map<?, ?>) items.get(0);
        assertThat(first.get("originalFilename")).isEqualTo("video-15.mp4");

        // Último da página 0 deve ser video-6.mp4 (15..6 = 10 itens)
        Map<?, ?> last = (Map<?, ?>) items.get(9);
        assertThat(last.get("originalFilename")).isEqualTo("video-6.mp4");
    }

    @Test
    void deve_retornar_200_com_lista_vazia_quando_usuario_nao_tiver_videos() {
        UUID userId = UUID.randomUUID();

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/videos?userId={userId}&page={page}&size={size}",
                Map.class,
                userId,
                0,
                10
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody().get("totalItems")).isEqualTo(0);
        assertThat(response.getBody().get("totalPages")).isEqualTo(0);

        Object itemsObj = response.getBody().get("items");
        assertThat(itemsObj).isInstanceOf(List.class);
        assertThat((List<?>) itemsObj).isEmpty();
    }

    @Test
    void deve_retornar_400_quando_userId_for_invalido() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/videos?userId={userId}&page={page}&size={size}",
                String.class,
                "user-invalido",
                0,
                10
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}