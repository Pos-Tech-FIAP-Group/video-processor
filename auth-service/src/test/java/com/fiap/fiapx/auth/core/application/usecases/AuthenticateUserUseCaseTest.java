package com.fiap.fiapx.auth.core.application.usecases;

import com.fiap.fiapx.auth.core.application.exception.InvalidCredentialsException;
import com.fiap.fiapx.auth.core.application.ports.PasswordEncoderPort;
import com.fiap.fiapx.auth.core.application.ports.TokenProviderPort;
import com.fiap.fiapx.auth.core.application.ports.UserRepositoryPort;
import com.fiap.fiapx.auth.core.domain.model.User;
import com.fiap.fiapx.auth.core.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserUseCaseTest {

    private static final long EXPIRES_MS = 3600000L;

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private TokenProviderPort tokenProvider;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    private AuthenticateUserUseCase useCase;

    private User userEnabled;

    @BeforeEach
    void setUp() {
        useCase = new AuthenticateUserUseCase(userRepository, tokenProvider, passwordEncoder, EXPIRES_MS);
        userEnabled = new User(
                "id1",
                UUID.randomUUID(),
                "bdd_user",
                "bdd@example.com",
                "hash",
                true,
                null,
                Set.of(UserRole.USER)
        );
    }

    @Nested
    @DisplayName("Quando credenciais são inválidas")
    class CredenciaisInvalidas {

        @Test
        @DisplayName("usuário não existe → InvalidCredentialsException")
        void usuario_nao_existe() {
            when(userRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute("inexistente", "senha123"))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessageContaining("Credenciais inválidas");
        }

        @Test
        @DisplayName("senha não confere → InvalidCredentialsException")
        void senha_errada() {
            when(userRepository.findByUsername("bdd_user")).thenReturn(Optional.of(userEnabled));
            when(passwordEncoder.matches("senhaErrada", "hash")).thenReturn(false);

            assertThatThrownBy(() -> useCase.execute("bdd_user", "senhaErrada"))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessageContaining("Credenciais inválidas");
        }

        @Test
        @DisplayName("usuário desabilitado → InvalidCredentialsException")
        void usuario_desabilitado() {
            userEnabled.setEnabled(false);
            when(userRepository.findByUsername("bdd_user")).thenReturn(Optional.of(userEnabled));
            when(passwordEncoder.matches("senha123", "hash")).thenReturn(true);

            assertThatThrownBy(() -> useCase.execute("bdd_user", "senha123"))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessageContaining("Usuário desabilitado");
        }
    }

    @Nested
    @DisplayName("Quando credenciais são válidas")
    class CredenciaisValidas {

        @Test
        @DisplayName("retorna AuthResult com token e userUuid")
        void retorna_auth_result() {
            when(userRepository.findByUsername("bdd_user")).thenReturn(Optional.of(userEnabled));
            when(passwordEncoder.matches("senha123", "hash")).thenReturn(true);
            when(tokenProvider.generateToken(userEnabled)).thenReturn("jwt-token");

            AuthResult result = useCase.execute("bdd_user", "senha123");

            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo("jwt-token");
            assertThat(result.username()).isEqualTo("bdd_user");
            assertThat(result.userUuid()).isEqualTo(userEnabled.getUserUuid().toString());
            assertThat(result.expiresInMs()).isEqualTo(EXPIRES_MS);
            verify(tokenProvider).generateToken(userEnabled);
        }
    }
}
