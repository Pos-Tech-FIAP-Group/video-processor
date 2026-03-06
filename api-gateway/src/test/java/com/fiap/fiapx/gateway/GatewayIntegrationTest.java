package com.fiap.fiapx.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewayIntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @DynamicPropertySource
    static void configureAuthServiceUrl(DynamicPropertyRegistry registry) {
        registry.add("auth.service-url", () -> "http://localhost:" + wireMockServer.port());
    }

    @Nested
    @DisplayName("Rota publica")
    class PublicRoute {

        @Test
        @DisplayName("GET /api/videos/health sem token nao retorna 401")
        void health_sem_token_nao_retorna_401() {
            webTestClient.get()
                    .uri("/api/videos/health")
                    .exchange()
                    .expectStatus()
                    .value(not(equalTo(401)));
        }

        @Test
        @DisplayName("GET /actuator/health sem token nao retorna 401")
        void actuator_health_sem_token_nao_retorna_401() {
            webTestClient.get()
                    .uri("/actuator/health")
                    .exchange()
                    .expectStatus()
                    .value(not(equalTo(401)));
        }
    }

    @Nested
    @DisplayName("Rota protegida")
    class ProtectedRoute {

        @Test
        @DisplayName("GET /api/auth/users/123 sem Authorization retorna 401")
        void sem_token_retorna_401() {
            webTestClient.get()
                    .uri("/api/auth/users/123")
                    .exchange()
                    .expectStatus()
                    .isUnauthorized()
                    .expectBody()
                    .jsonPath("$.message").isNotEmpty();
        }

        @Test
        @DisplayName("GET /api/auth/users/123 com token invalido (validate=false) retorna 401")
        void token_invalido_retorna_401() {
            wireMockServer.stubFor(
                    get(urlPathEqualTo("/api/auth/validate"))
                            .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                                    .withBody("{\"valid\":false,\"subject\":null}"))
            );

            webTestClient.get()
                    .uri("/api/auth/users/123")
                    .header("Authorization", "Bearer invalid-token")
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @Test
        @DisplayName("GET /api/auth/users/123 com token valido (validate=true) nao retorna 401")
        void token_valido_nao_retorna_401() {
            wireMockServer.stubFor(
                    get(urlPathEqualTo("/api/auth/validate"))
                            .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                                    .withBody("{\"valid\":true,\"subject\":\"user1\"}"))
            );

            webTestClient.get()
                    .uri("/api/auth/users/123")
                    .header("Authorization", "Bearer valid-token")
                    .exchange()
                    .expectStatus()
                    .value(not(equalTo(401)));
        }
    }
}
