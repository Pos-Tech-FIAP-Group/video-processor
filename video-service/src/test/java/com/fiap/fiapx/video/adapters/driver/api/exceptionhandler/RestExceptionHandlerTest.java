package com.fiap.fiapx.video.adapters.driver.api.exceptionhandler;

import com.fiap.fiapx.video.core.application.exceptions.VideoNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    void deve_retornar_not_found_padronizado() {
        UUID id = UUID.randomUUID();

        ResponseEntity<RestExceptionHandler.ErrorResponse> response =
                handler.handleVideoNotFound(new VideoNotFoundException(id));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().code()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().message()).contains(id.toString());
        assertThat(response.getBody().timestamp()).isNotNull();
    }
}
