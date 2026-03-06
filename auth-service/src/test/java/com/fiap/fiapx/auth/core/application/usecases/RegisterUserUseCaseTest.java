package com.fiap.fiapx.auth.core.application.usecases;

import com.fiap.fiapx.auth.core.application.exception.UserAlreadyExistsException;
import com.fiap.fiapx.auth.core.application.ports.PasswordEncoderPort;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterUserUseCase(userRepository, passwordEncoder);
    }

    @Nested
    @DisplayName("Quando username ou email já existem")
    class Duplicados {

        @Test
        @DisplayName("username já existe → lança UserAlreadyExistsException")
        void username_ja_existe() {
            when(userRepository.findByUsername("existente")).thenReturn(Optional.of(new User()));

            assertThatThrownBy(() -> useCase.execute("existente", "a@b.com", "senha123"))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("Username já existe");
            verify(userRepository).findByUsername("existente");
        }

        @Test
        @DisplayName("email já existe → lança UserAlreadyExistsException")
        void email_ja_existe() {
            when(userRepository.findByUsername("novo")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("existente@b.com")).thenReturn(Optional.of(new User()));

            assertThatThrownBy(() -> useCase.execute("novo", "existente@b.com", "senha123"))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("Email já existe");
        }
    }

    @Nested
    @DisplayName("Quando dados são válidos")
    class DadosValidos {

        @Test
        @DisplayName("salva usuário e retorna User com userUuid")
        void salva_e_retorna_user() {
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(passwordEncoder.encode("senha123")).thenReturn("hash");
            User saved = new User(
                    "id1",
                    UUID.randomUUID(),
                    "bdd_user",
                    "bdd@example.com",
                    "hash",
                    true,
                    LocalDateTime.now(),
                    Set.of(UserRole.USER)
            );
            when(userRepository.save(any(User.class))).thenReturn(saved);

            User result = useCase.execute("bdd_user", "bdd@example.com", "senha123");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("id1");
            assertThat(result.getUserUuid()).isNotNull();
            assertThat(result.getUsername()).isEqualTo("bdd_user");
            assertThat(result.getEmail()).isEqualTo("bdd@example.com");
            verify(passwordEncoder).encode("senha123");
            verify(userRepository).save(any(User.class));
        }
    }
}
