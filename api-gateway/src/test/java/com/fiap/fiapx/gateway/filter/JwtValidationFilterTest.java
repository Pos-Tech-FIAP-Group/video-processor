package com.fiap.fiapx.gateway.filter;

import com.fiap.fiapx.gateway.client.TokenValidationClient;
import com.fiap.fiapx.gateway.client.ValidateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtValidationFilterTest {

    @Mock
    private TokenValidationClient tokenValidationClient;

    @Mock
    private org.springframework.cloud.gateway.filter.GatewayFilterChain chain;

    private JwtValidationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtValidationFilter(tokenValidationClient);
        lenient().when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    @Nested
    @DisplayName("Quando a rota é pública ou OPTIONS")
    class PublicPathOrOptions {

        @Test
        @DisplayName("OPTIONS passa sem validar token")
        void options_passa_sem_validar() {
            ServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.method(HttpMethod.OPTIONS, "/api/videos")
            );

            Mono<Void> result = filter.filter(exchange, chain);

            result.block();
            verify(chain).filter(exchange);
            assertThat(exchange.getResponse().getStatusCode()).isNull();
        }

        @Test
        @DisplayName("GET /api/auth/login passa sem Authorization")
        void public_path_login_passa() {
            ServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/auth/login")
            );

            Mono<Void> result = filter.filter(exchange, chain);

            result.block();
            verify(chain).filter(exchange);
        }

        @Test
        @DisplayName("GET /api/videos/health passa sem Authorization")
        void public_path_health_passa() {
            ServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/videos/health")
            );

            Mono<Void> result = filter.filter(exchange, chain);

            result.block();
            verify(chain).filter(exchange);
        }

        @Test
        @DisplayName("GET /actuator/health passa sem Authorization")
        void public_path_actuator_passa() {
            ServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/actuator/health")
            );

            Mono<Void> result = filter.filter(exchange, chain);

            result.block();
            verify(chain).filter(exchange);
        }
    }

    @Nested
    @DisplayName("Quando a rota é protegida")
    class ProtectedPath {

        @Test
        @DisplayName("sem header Authorization retorna 401")
        void sem_token_retorna_401() {
            ServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/auth/users/123")
            );

            Mono<Void> result = filter.filter(exchange, chain);

            result.block();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(exchange.getResponse().getHeaders().getContentType()).isNotNull();
        }

        @Test
        @DisplayName("com Authorization sem Bearer retorna 401")
        void token_sem_bearer_retorna_401() {
            ServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/auth/users/123")
                            .header(HttpHeaders.AUTHORIZATION, "Basic xyz")
            );

            Mono<Void> result = filter.filter(exchange, chain);

            result.block();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("com token inválido (valid=false) retorna 401")
        void token_invalido_retorna_401() {
            when(tokenValidationClient.validate("meu-token"))
                    .thenReturn(new ValidateResponse(false, null));

            ServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/auth/users/123")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer meu-token")
            );

            Mono<Void> result = filter.filter(exchange, chain);

            result.block();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("com token válido (valid=true) chama chain.filter")
        void token_valido_chama_chain() {
            when(tokenValidationClient.validate("token-ok"))
                    .thenReturn(new ValidateResponse(true, "user1"));

            ServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/auth/users/123")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer token-ok")
            );

            Mono<Void> result = filter.filter(exchange, chain);

            result.block();
            verify(chain).filter(exchange);
            assertThat(exchange.getResponse().getStatusCode()).isNull();
        }
    }

    @Nested
    @DisplayName("Order")
    class Order {

        @Test
        @DisplayName("retorna HIGHEST_PRECEDENCE")
        void order_is_highest_precedence() {
            assertThat(filter.getOrder()).isEqualTo(org.springframework.core.Ordered.HIGHEST_PRECEDENCE);
        }
    }
}
