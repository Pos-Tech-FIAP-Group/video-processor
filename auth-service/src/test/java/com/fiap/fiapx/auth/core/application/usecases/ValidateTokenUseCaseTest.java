package com.fiap.fiapx.auth.core.application.usecases;

import com.fiap.fiapx.auth.core.application.ports.TokenProviderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateTokenUseCaseTest {

    @Mock
    private TokenProviderPort tokenProvider;

    private ValidateTokenUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ValidateTokenUseCase(tokenProvider);
    }

    @Test
    @DisplayName("token válido → Optional com subject")
    void token_valido_retorna_subject() {
        when(tokenProvider.isValid("valid-token")).thenReturn(true);
        when(tokenProvider.getSubject("valid-token")).thenReturn(Optional.of("bdd_user"));

        Optional<String> result = useCase.execute("valid-token");

        assertThat(result).hasValue("bdd_user");
    }

    @Test
    @DisplayName("token inválido → Optional.empty")
    void token_invalido_retorna_empty() {
        when(tokenProvider.isValid("invalid-token")).thenReturn(false);

        Optional<String> result = useCase.execute("invalid-token");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("token null ou em branco → Optional.empty")
    void token_null_ou_blank_retorna_empty() {
        assertThat(useCase.execute(null)).isEmpty();
        assertThat(useCase.execute("")).isEmpty();
        assertThat(useCase.execute("   ")).isEmpty();
    }
}
