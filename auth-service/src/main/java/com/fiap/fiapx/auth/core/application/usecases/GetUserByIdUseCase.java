package com.fiap.fiapx.auth.core.application.usecases;

import com.fiap.fiapx.auth.core.application.ports.UserRepositoryPort;
import com.fiap.fiapx.auth.core.domain.model.User;

import java.util.Optional;

/**
 * Caso de uso: obter usuário por id. Para GET /auth/users/{id}.
 * O controller deve expor apenas dados seguros (sem passwordHash).
 */
public class GetUserByIdUseCase {

    private final UserRepositoryPort userRepository;

    public GetUserByIdUseCase(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> execute(String id) {
        return userRepository.findById(id);
    }
}
