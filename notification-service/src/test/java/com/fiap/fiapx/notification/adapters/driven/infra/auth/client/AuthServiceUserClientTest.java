package com.fiap.fiapx.notification.adapters.driven.infra.auth.client;

import com.fiap.fiapx.notification.core.domain.model.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AuthServiceUserClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private AuthServiceUserClient client;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        client = new AuthServiceUserClient(restTemplate, "http://auth-service:8081/");
    }

    @Test
    void retorna_empty_quando_userUuid_null() {
        Optional<UserInfo> result = client.findByUserUuid(null);

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void retorna_empty_quando_userUuid_em_branco() {
        Optional<UserInfo> result = client.findByUserUuid("  ");

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void retorna_userInfo_quando_auth_retorna_200() {
        String json = """
                {"id":"id1","userUuid":"uuid-123","username":"john","email":"john@example.com","enabled":true}
                """;
        mockServer.expect(requestTo("http://auth-service:8081/internal/users/by-uuid/uuid-123"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Optional<UserInfo> result = client.findByUserUuid("uuid-123");

        assertThat(result).isPresent();
        assertThat(result.get().email()).isEqualTo("john@example.com");
        assertThat(result.get().username()).isEqualTo("john");
        mockServer.verify();
    }

    @Test
    void retorna_empty_quando_auth_retorna_404() {
        mockServer.expect(requestTo("http://auth-service:8081/internal/users/by-uuid/not-found"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        Optional<UserInfo> result = client.findByUserUuid("not-found");

        assertThat(result).isEmpty();
        mockServer.verify();
    }
}
