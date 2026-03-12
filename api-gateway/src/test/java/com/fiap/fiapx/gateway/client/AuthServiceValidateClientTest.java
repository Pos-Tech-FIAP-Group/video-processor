package com.fiap.fiapx.gateway.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceValidateClientTest {

    private WireMockServer wireMockServer;
    private AuthServiceValidateClient client;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        client = new AuthServiceValidateClient(
                "http://localhost:" + wireMockServer.port(),
                new ObjectMapper()
        );
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Nested
    @DisplayName("Quando o Auth Service retorna 200")
    class QuandoRetorna200 {

        @Test
        @DisplayName("com valid=true e subject retorna ValidateResponse válido")
        void valid_true_com_subject() {
            wireMockServer.stubFor(
                    get(urlPathEqualTo("/api/auth/validate"))
                            .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("{\"valid\":true,\"subject\":\"user-123\"}"))
            );

            ValidateResponse result = client.validate("meu-jwt");

            assertThat(result.valid()).isTrue();
            assertThat(result.subject()).isEqualTo("user-123");
        }

        @Test
        @DisplayName("com valid=false retorna ValidateResponse inválido")
        void valid_false() {
            wireMockServer.stubFor(
                    get(urlPathEqualTo("/api/auth/validate"))
                            .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("{\"valid\":false,\"subject\":null}"))
            );

            ValidateResponse result = client.validate("token-invalido");

            assertThat(result.valid()).isFalse();
            assertThat(result.subject()).isNull();
        }

        @Test
        @DisplayName("codifica o token na query string")
        void codifica_token_na_url() {
            wireMockServer.stubFor(
                    get(urlPathEqualTo("/api/auth/validate"))
                            .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("{\"valid\":true,\"subject\":\"ok\"}"))
            );

            ValidateResponse result = client.validate("token+com/espaco");

            assertThat(result.valid()).isTrue();
            assertThat(result.subject()).isEqualTo("ok");
        }
    }

    @Nested
    @DisplayName("Quando o Auth Service retorna status diferente de 200")
    class QuandoStatusDiferenteDe200 {

        @Test
        @DisplayName("401 retorna ValidateResponse inválido")
        void status_401() {
            wireMockServer.stubFor(
                    get(urlPathEqualTo("/api/auth/validate"))
                            .willReturn(aResponse().withStatus(401))
            );

            ValidateResponse result = client.validate("qualquer");

            assertThat(result.valid()).isFalse();
            assertThat(result.subject()).isNull();
        }

        @Test
        @DisplayName("500 retorna ValidateResponse inválido")
        void status_500() {
            wireMockServer.stubFor(
                    get(urlPathEqualTo("/api/auth/validate"))
                            .willReturn(aResponse().withStatus(500))
            );

            ValidateResponse result = client.validate("qualquer");

            assertThat(result.valid()).isFalse();
            assertThat(result.subject()).isNull();
        }
    }

    @Nested
    @DisplayName("Quando ocorre erro na chamada HTTP")
    class QuandoErroNaChamada {

        @Test
        @DisplayName("resposta com JSON inválido retorna ValidateResponse inválido")
        void json_invalido() {
            wireMockServer.stubFor(
                    get(urlPathEqualTo("/api/auth/validate"))
                            .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("not json"))
            );

            ValidateResponse result = client.validate("qualquer");

            assertThat(result.valid()).isFalse();
            assertThat(result.subject()).isNull();
        }

        @Test
        @DisplayName("servidor indisponível retorna ValidateResponse inválido")
        void servidor_indisponivel() {
            wireMockServer.stop();
            wireMockServer = null;

            ValidateResponse result = client.validate("qualquer");

            assertThat(result.valid()).isFalse();
            assertThat(result.subject()).isNull();
        }
    }
}
