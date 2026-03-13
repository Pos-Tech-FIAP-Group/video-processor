package com.fiap.fiapx.video.infra.api;

import com.fiap.fiapx.video.adapters.driver.api.dto.response.VideoResponse;
import com.fiap.fiapx.video.adapters.driver.api.exceptionhandler.RestExceptionHandler;
import com.fiap.fiapx.video.config.TestSecurityConfig;
import com.fiap.fiapx.video.core.domain.enums.VideoStatus;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class VideoGetByIdIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String JWT_SECRET = "dev-secret-key-change-in-production-min-256-bits";

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
                "PROCESSANDO",
                null,
                null,
                now,
                now,
                null
        );

        ResponseEntity<VideoResponse> response = restTemplate.exchange(
                "/api/videos/{id}",
                HttpMethod.GET,
                entidadeComToken(),
                VideoResponse.class,
                videoId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(videoId);
        assertThat(response.getBody().userId()).isEqualTo(userId);
        assertThat(response.getBody().status()).isEqualTo(VideoStatus.PROCESSANDO);
    }

    @Test
    void deve_retornar_404_quando_video_nao_existir() {
        UUID inexistente = UUID.randomUUID();

        ResponseEntity<RestExceptionHandler.ErrorResponse> response = restTemplate.exchange(
                "/api/videos/{id}",
                HttpMethod.GET,
                entidadeComToken(),
                RestExceptionHandler.ErrorResponse.class,
                inexistente
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();

        var body = response.getBody();
        assertThat(body.status()).isEqualTo(404);
        assertThat(body.code()).isEqualTo("NOT_FOUND");
        assertThat(body.message()).contains(inexistente.toString());
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void deve_retornar_400_quando_uuid_for_invalido() {
        ResponseEntity<RestExceptionHandler.ErrorResponse> response = restTemplate.exchange(
                "/api/videos/{id}",
                HttpMethod.GET,
                entidadeComToken(),
                RestExceptionHandler.ErrorResponse.class,
                "id-invalido"
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        var body = response.getBody();
        assertThat(body.status()).isEqualTo(400);
        assertThat(body.timestamp()).isNotNull();
    }

    private HttpEntity<Void> entidadeComToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(gerarTokenValido());
        return new HttpEntity<>(headers);
    }

    private String gerarTokenValido() {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject("usuario-teste")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }
}