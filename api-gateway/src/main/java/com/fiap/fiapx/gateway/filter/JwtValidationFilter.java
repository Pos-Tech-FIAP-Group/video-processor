package com.fiap.fiapx.gateway.filter;

import com.fiap.fiapx.gateway.client.ValidateResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Filtro que valida JWT em rotas protegidas chamando o Auth Service GET /api/auth/validate.
 * Rotas públicas (/api/auth/register, /api/auth/login, /api/auth/validate) não passam pela validação.
 */
@Component
public class JwtValidationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/validate",
            "/actuator/"
    );

    private final String authServiceUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public JwtValidationFilter(
            @Value("${auth.service-url:http://localhost:8081}") String authServiceUrl,
            ObjectMapper objectMapper) {
        this.authServiceUrl = authServiceUrl.replaceAll("/$", "");
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String token = extractBearerToken(exchange.getRequest().getHeaders());
        if (token == null || token.isBlank()) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        // java.net.http.HttpClient para chamar o Auth (sem contexto reativo do Gateway = sempre 8081)
        String validateUrl = authServiceUrl + "/api/auth/validate?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        return Mono.fromCallable(() -> callAuthValidate(validateUrl))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    log.warn("Auth validate call failed", e);
                    return Mono.just(new ValidateResponse(false, null));
                })
                .flatMap(response -> response != null && response.valid()
                        ? chain.filter(exchange)
                        : unauthorized(exchange, "Invalid token"));
    }

    private ValidateResponse callAuthValidate(String validateUrl) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(validateUrl))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            return new ValidateResponse(false, null);
        }
        return objectMapper.readValue(response.body(), ValidateResponse.class);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private String extractBearerToken(HttpHeaders headers) {
        String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"message\":\"" + escapeJson(message) + "\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
