package com.fiap.fiapx.auth.core.application.usecases;

import com.fiap.fiapx.auth.core.application.exception.InvalidCredentialsException;
import com.fiap.fiapx.auth.core.application.ports.PasswordEncoderPort;
import com.fiap.fiapx.auth.core.application.ports.TokenProviderPort;
import com.fiap.fiapx.auth.core.application.ports.UserRepositoryPort;
import com.fiap.fiapx.auth.core.domain.model.User;

/**
 * Caso de uso: autenticação (login). Valida credenciais, gera JWT e retorna token + dados mínimos.
 */
public class AuthenticateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final TokenProviderPort tokenProvider;
    private final PasswordEncoderPort passwordEncoder;
    private final long expiresInMs;

    public AuthenticateUserUseCase(UserRepositoryPort userRepository,
                                   TokenProviderPort tokenProvider,
                                   PasswordEncoderPort passwordEncoder,
                                   long expiresInMs) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.expiresInMs = expiresInMs;
    }

    public AuthResult execute(String username, String plainPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Credenciais inválidas"));

        if (!passwordEncoder.matches(plainPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciais inválidas");
        }

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("Usuário desabilitado");
        }

        String token = tokenProvider.generateToken(user);
        String userUuid = user.getUserUuid() != null ? user.getUserUuid().toString() : null;
        return new AuthResult(token, user.getUsername(), expiresInMs, user.getId(), userUuid);
    }
}
