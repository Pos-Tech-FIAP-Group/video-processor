package com.fiap.fiapx.auth.adapters.driven.infra.security.adapter;

import com.fiap.fiapx.auth.core.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider")
class JwtTokenProviderTest {

    private static final long EXPIRATION_MS = 3600_000L;

    private JwtTokenProvider provider;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");
    }

    @Nested
    @DisplayName("secret curto (< 32 bytes)")
    class SecretCurto {

        @Test
        @DisplayName("aceita secret curto e gera token válido")
        void secret_curto_gera_token_valido() {
            provider = new JwtTokenProvider("short", EXPIRATION_MS);

            String token = provider.generateToken(user);

            assertThat(token).isNotBlank();
            assertThat(provider.isValid(token)).isTrue();
            assertThat(provider.getSubject(token)).isEqualTo(Optional.of("testuser"));
        }
    }

    @Nested
    @DisplayName("token inválido")
    class TokenInvalido {

        @BeforeEach
        void setUpProvider() {
            provider = new JwtTokenProvider("secret-muito-longo-para-hmac-sha256-minimo-32", EXPIRATION_MS);
        }

        @Test
        @DisplayName("isValid retorna false para token inválido")
        void isValid_retorna_false() {
            assertThat(provider.isValid("token-invalido-ou-malformado")).isFalse();
            assertThat(provider.isValid("")).isFalse();
            assertThat(provider.isValid("eyJhbGciOiJIUzI1NiJ9.invalid.signature")).isFalse();
        }

        @Test
        @DisplayName("getSubject retorna empty para token inválido")
        void getSubject_retorna_empty() {
            assertThat(provider.getSubject("token-invalido")).isEmpty();
            assertThat(provider.getSubject("")).isEmpty();
        }
    }
}
