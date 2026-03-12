package com.fiap.fiapx.gateway.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Implementação que valida o token chamando o Auth Service GET /api/auth/validate.
 */
@Component
public class AuthServiceValidateClient implements TokenValidationClient {

    private final String authServiceUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AuthServiceValidateClient(
            @Value("${auth.service-url:http://localhost:8081}") String authServiceUrl,
            ObjectMapper objectMapper) {
        this.authServiceUrl = authServiceUrl.replaceAll("/$", "");
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .build();
    }

    @Override
    public ValidateResponse validate(String token) {
        try {
            String validateUrl = authServiceUrl + "/api/auth/validate?token="
                    + URLEncoder.encode(token, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(validateUrl))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                return new ValidateResponse(false, null);
            }
            return objectMapper.readValue(response.body(), ValidateResponse.class);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ValidateResponse(false, null);
        } catch (Exception e) {
            return new ValidateResponse(false, null);
        }
    }
}
