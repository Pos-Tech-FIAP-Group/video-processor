package com.fiap.fiapx.auth.core.domain.model;

import com.fiap.fiapx.auth.core.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Entidade de domínio: usuário do sistema.
 * Sem anotações de framework (JPA/Mongo); usado apenas no core.
 * userUuid: identificador externo (UUID) para uso em outros serviços (ex.: video-service).
 */
public class User {

    private String id;
    private UUID userUuid;
    private String username;
    private String email;
    private String passwordHash;
    private boolean enabled;
    private LocalDateTime createdAt;
    private Set<UserRole> roles;

    public User() {
    }

    public User(String id, UUID userUuid, String username, String email, String passwordHash,
                boolean enabled, LocalDateTime createdAt, Set<UserRole> roles) {
        this.id = id;
        this.userUuid = userUuid;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.roles = roles;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UUID getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(UUID userUuid) {
        this.userUuid = userUuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }
}
