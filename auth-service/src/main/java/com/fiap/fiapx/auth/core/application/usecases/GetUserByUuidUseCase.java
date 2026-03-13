package com.fiap.fiapx.auth.core.application.usecases;

import com.fiap.fiapx.auth.core.application.ports.UserRepositoryPort;
import com.fiap.fiapx.auth.core.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso: obter usuário por userUuid (UUID externo).
 * Para GET /internal/users/by-uuid/{uuid} — uso service-to-service (ex.: notification).
 */
public class GetUserByUuidUseCase {

    private final UserRepositoryPort userRepository;

    public GetUserByUuidUseCase(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> execute(UUID userUuid) {
        return userRepository.findByUserUuid(userUuid);
    }
}
