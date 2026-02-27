package com.fiap.fiapx.auth.core.application.ports;

import com.fiap.fiapx.auth.core.domain.model.User;

import java.util.Optional;

/**
 * Porta de persistência de usuários. Implementada no adapter driven (MongoDB).
 */
public interface UserRepositoryPort {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    User save(User user);

    Optional<User> findById(String id);
}
