package com.fiap.fiapx.processing.adapters.driven.infra.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garante que o payload serializado para a fila contém os campos esperados pelos consumidores
 * (video-service e notification-service), incluindo userId em eventos de falha.
 */
class VideoProcessingCompletedPayloadSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void payload_completed_serializa_com_userId_null_para_consumidores_nao_quebrarem() throws Exception {
        var payload = new RabbitMqProcessingEventPublisher.VideoProcessingCompletedPayload(
                "video-123",
                true,
                42,
                "https://bucket/video-123.zip",
                null,
                null
        );

        String json = objectMapper.writeValueAsString(payload);
        JsonNode node = objectMapper.readTree(json);

        assertThat(node.has("videoId")).isTrue();
        assertThat(node.get("videoId").asText()).isEqualTo("video-123");
        assertThat(node.has("success")).isTrue();
        assertThat(node.get("success").asBoolean()).isTrue();
        assertThat(node.has("frameCount")).isTrue();
        assertThat(node.has("zipPath")).isTrue();
        assertThat(node.has("errorMessage")).isTrue();
        assertThat(node.has("userId")).isTrue();
        assertThat(node.get("userId").isNull()).isTrue();
    }

    @Test
    void payload_failed_serializa_com_userId_preenchido_para_notification_consumir() throws Exception {
        var payload = new RabbitMqProcessingEventPublisher.VideoProcessingCompletedPayload(
                "video-456",
                false,
                null,
                null,
                "Erro no processamento",
                "user-uuid-789"
        );

        String json = objectMapper.writeValueAsString(payload);
        JsonNode node = objectMapper.readTree(json);

        assertThat(node.get("videoId").asText()).isEqualTo("video-456");
        assertThat(node.get("success").asBoolean()).isFalse();
        assertThat(node.get("errorMessage").asText()).isEqualTo("Erro no processamento");
        assertThat(node.has("userId")).isTrue();
        assertThat(node.get("userId").asText()).isEqualTo("user-uuid-789");
    }
}
