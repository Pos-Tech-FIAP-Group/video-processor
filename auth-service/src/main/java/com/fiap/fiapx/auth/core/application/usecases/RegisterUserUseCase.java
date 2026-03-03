package com.fiap.fiapx.auth.core.application.usecases;

import com.fiap.fiapx.auth.core.application.exception.UserAlreadyExistsException;
import com.fiap.fiapx.auth.core.application.ports.PasswordEncoderPort;
import com.fiap.fiapx.auth.core.application.ports.UserRepositoryPort;
import com.fiap.fiapx.auth.core.domain.model.User;
import com.fiap.fiapx.auth.core.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Caso de uso: registro de novo usuário. Criptografa senha, valida duplicidade e persiste.
 */
public class RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    public RegisterUserUseCase(UserRepositoryPort userRepository, PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User execute(String username, String email, String plainPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("Username já existe: " + username);
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Email já existe: " + email);
        }

        String passwordHash = passwordEncoder.encode(plainPassword);
        User user = new User(
                null,
                UUID.randomUUID(),
                username,
                email,
                passwordHash,
                true,
                LocalDateTime.now(),
                Set.of(UserRole.USER)
        );
        return userRepository.save(user);
    }
}
