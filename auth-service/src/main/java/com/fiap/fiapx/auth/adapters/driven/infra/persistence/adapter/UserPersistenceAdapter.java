package com.fiap.fiapx.auth.adapters.driven.infra.persistence.adapter;

import com.fiap.fiapx.auth.adapters.driven.infra.persistence.entity.UserDocument;
import com.fiap.fiapx.auth.adapters.driven.infra.persistence.repository.UserMongoRepository;
import com.fiap.fiapx.auth.core.application.ports.UserRepositoryPort;
import com.fiap.fiapx.auth.core.domain.model.User;
import com.fiap.fiapx.auth.core.domain.enums.UserRole;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserMongoRepository repository;

    public UserPersistenceAdapter(UserMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username).map(this::toUser);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toUser);
    }

    @Override
    public User save(User user) {
        UserDocument doc = toDocument(user);
        UserDocument saved = repository.save(doc);
        return toUser(saved);
    }

    @Override
    public Optional<User> findById(String id) {
        return repository.findById(id).map(this::toUser);
    }

    private User toUser(UserDocument doc) {
        Set<UserRole> roles = doc.getRoles() == null
                ? Collections.emptySet()
                : doc.getRoles().stream()
                        .map(UserRole::valueOf)
                        .collect(Collectors.toSet());
        UUID userUuid = doc.getUserUuid() != null ? UUID.fromString(doc.getUserUuid()) : null;
        return new User(
                doc.getId(),
                userUuid,
                doc.getUsername(),
                doc.getEmail(),
                doc.getPasswordHash(),
                doc.isEnabled(),
                doc.getCreatedAt(),
                roles
        );
    }

    private UserDocument toDocument(User user) {
        UserDocument doc = new UserDocument();
        doc.setId(user.getId());
        doc.setUserUuid(user.getUserUuid() != null ? user.getUserUuid().toString() : null);
        doc.setUsername(user.getUsername());
        doc.setEmail(user.getEmail());
        doc.setPasswordHash(user.getPasswordHash());
        doc.setEnabled(user.isEnabled());
        doc.setCreatedAt(user.getCreatedAt());
        if (user.getRoles() != null) {
            doc.setRoles(user.getRoles().stream()
                    .map(UserRole::name)
                    .collect(Collectors.toList()));
        }
        return doc;
    }
}
