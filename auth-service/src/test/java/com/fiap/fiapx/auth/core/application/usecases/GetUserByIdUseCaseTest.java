package com.fiap.fiapx.auth.core.application.usecases;

import com.fiap.fiapx.auth.core.application.ports.UserRepositoryPort;
import com.fiap.fiapx.auth.core.domain.model.User;
import com.fiap.fiapx.auth.core.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserByIdUseCaseTest {

    @Mock
    private UserRepositoryPort userRepository;

    private GetUserByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetUserByIdUseCase(userRepository);
    }

    @Test
    @DisplayName("id existe → Optional com User")
    void id_existe_retorna_user() {
        User user = new User(
                "id1",
                UUID.randomUUID(),
                "bdd_user",
                "bdd@example.com",
                "hash",
                true,
                null,
                Set.of(UserRole.USER)
        );
        when(userRepository.findById("id1")).thenReturn(Optional.of(user));

        Optional<User> result = useCase.execute("id1");

        assertThat(result).hasValueSatisfying(u -> {
            assertThat(u.getId()).isEqualTo("id1");
            assertThat(u.getUsername()).isEqualTo("bdd_user");
        });
    }

    @Test
    @DisplayName("id não existe → Optional.empty")
    void id_nao_existe_retorna_empty() {
        when(userRepository.findById("inexistente")).thenReturn(Optional.empty());

        Optional<User> result = useCase.execute("inexistente");

        assertThat(result).isEmpty();
    }
}
