package com.fiap.fiapx.auth.adapters.driver.api.controller;

import com.fiap.fiapx.auth.adapters.driver.api.dto.response.AuthResponse;
import com.fiap.fiapx.auth.adapters.driver.api.dto.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class AuthControllerIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDB = new MongoDBContainer("mongo:7");

    @Autowired
    private TestRestTemplate restTemplate;

    @Nested
    @DisplayName("POST /api/auth/register")
    class Register {

        @Test
        @DisplayName("body válido → 201 e corpo com id, userUuid, username, email")
        void body_valido_retorna_201() {
            String body = """
                    {"username":"int_user1","email":"int1@example.com","password":"senha123"}
                    """;
            ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                    "/api/auth/register",
                    request(body),
                    UserResponse.class
            );
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().username()).isEqualTo("int_user1");
            assertThat(response.getBody().email()).isEqualTo("int1@example.com");
            assertThat(response.getBody().userUuid()).isNotNull();
            assertThat(response.getBody().id()).isNotNull();
        }

        @Test
        @DisplayName("username duplicado → 400")
        void username_duplicado_retorna_400() {
            String body = """
                    {"username":"dup_user","email":"dup1@example.com","password":"senha123"}
                    """;
            restTemplate.postForEntity("/api/auth/register", request(body), UserResponse.class);

            ResponseEntity<String> second = restTemplate.postForEntity(
                    "/api/auth/register",
                    request("{\"username\":\"dup_user\",\"email\":\"outro@example.com\",\"password\":\"senha123\"}"),
                    String.class
            );
            assertThat(second.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("credenciais corretas → 200 e token, username, userUuid")
        void credenciais_corretas_retorna_200() {
            String registerBody = """
                    {"username":"login_user","email":"login@example.com","password":"senha123"}
                    """;
            restTemplate.postForEntity("/api/auth/register", request(registerBody), UserResponse.class);

            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                    "/api/auth/login",
                    request("{\"username\":\"login_user\",\"password\":\"senha123\"}"),
                    AuthResponse.class
            );
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().token()).isNotBlank();
            assertThat(response.getBody().username()).isEqualTo("login_user");
            assertThat(response.getBody().userUuid()).isNotNull();
        }

        @Test
        @DisplayName("senha errada → 401")
        void senha_errada_retorna_401() {
            String registerBody = """
                    {"username":"wrong_pwd_user","email":"wrong@example.com","password":"senha123"}
                    """;
            restTemplate.postForEntity("/api/auth/register", request(registerBody), UserResponse.class);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/api/auth/login",
                    request("{\"username\":\"wrong_pwd_user\",\"password\":\"senhaErrada\"}"),
                    String.class
            );
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("GET /api/auth/validate")
    class Validate {

        @Test
        @DisplayName("token válido → 200 e valid=true")
        void token_valido_retorna_200_valid_true() {
            String registerBody = """
                    {"username":"val_user","email":"val@example.com","password":"senha123"}
                    """;
            restTemplate.postForEntity("/api/auth/register", request(registerBody), UserResponse.class);

            ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                    "/api/auth/login",
                    request("{\"username\":\"val_user\",\"password\":\"senha123\"}"),
                    AuthResponse.class
            );
            String token = loginResponse.getBody() != null ? loginResponse.getBody().token() : null;

            ResponseEntity<ValidateResponse> response = restTemplate.getForEntity(
                    "/api/auth/validate?token=" + token,
                    ValidateResponse.class
            );
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().valid()).isTrue();
            assertThat(response.getBody().subject()).isEqualTo("val_user");
        }

        @Test
        @DisplayName("token inválido → 200 e valid=false, subject=null")
        void token_invalido_retorna_200_valid_false() {
            ResponseEntity<ValidateResponse> response = restTemplate.getForEntity(
                    "/api/auth/validate?token=token-invalido-ou-expirado",
                    ValidateResponse.class
            );
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().valid()).isFalse();
            assertThat(response.getBody().subject()).isNull();
        }
    }

    @Nested
    @DisplayName("GET /api/auth/users/{id}")
    class GetUserById {

        @Test
        @DisplayName("id existente com token → 200")
        void id_existente_com_token_retorna_200() {
            String registerBody = """
                    {"username":"get_user","email":"get@example.com","password":"senha123"}
                    """;
            ResponseEntity<UserResponse> reg = restTemplate.postForEntity("/api/auth/register", request(registerBody), UserResponse.class);
            String userId = reg.getBody() != null ? reg.getBody().id() : null;

            ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                    "/api/auth/login",
                    request("{\"username\":\"get_user\",\"password\":\"senha123\"}"),
                    AuthResponse.class
            );
            String token = loginResponse.getBody() != null ? loginResponse.getBody().token() : null;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<UserResponse> response = restTemplate.exchange(
                    "/api/auth/users/" + userId,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    UserResponse.class
            );
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().username()).isEqualTo("get_user");
        }

        @Test
        @DisplayName("id inexistente com token → 404")
        void id_inexistente_retorna_404() {
            String registerBody = """
                    {"username":"for404","email":"for404@example.com","password":"senha123"}
                    """;
            restTemplate.postForEntity("/api/auth/register", request(registerBody), UserResponse.class);

            ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                    "/api/auth/login",
                    request("{\"username\":\"for404\",\"password\":\"senha123\"}"),
                    AuthResponse.class
            );
            String token = loginResponse.getBody() != null ? loginResponse.getBody().token() : null;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/auth/users/507f1f77bcf86cd799439011",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    private static HttpEntity<String> request(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(json, headers);
    }

    private record ValidateResponse(boolean valid, String subject) {
    }
}
