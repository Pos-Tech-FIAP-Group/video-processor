package com.fiap.fiapx.auth.core.application.usecases;

import com.fiap.fiapx.auth.core.application.ports.TokenProviderPort;

import java.util.Optional;

/**
 * Caso de uso: validar token JWT e extrair subject (ex.: username ou id).
 * Útil para endpoint GET /auth/validate ou validação no Gateway.
 */
public class ValidateTokenUseCase {

    private final TokenProviderPort tokenProvider;

    public ValidateTokenUseCase(TokenProviderPort tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    public Optional<String> execute(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        if (!tokenProvider.isValid(token)) {
            return Optional.empty();
        }
        return tokenProvider.getSubject(token);
    }
}
