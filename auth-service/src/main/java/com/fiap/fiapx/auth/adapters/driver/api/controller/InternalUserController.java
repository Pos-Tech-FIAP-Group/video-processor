package com.fiap.fiapx.auth.adapters.driver.api.controller;

import com.fiap.fiapx.auth.adapters.driver.api.dto.response.UserResponse;
import com.fiap.fiapx.auth.core.application.usecases.GetUserByUuidUseCase;
import com.fiap.fiapx.auth.core.domain.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Endpoints internos para uso service-to-service (ex.: notification-service).
 * Não expostos no gateway; acesso apenas direto ao auth-service (rede interna).
 */
@RestController
@RequestMapping("/internal")
public class InternalUserController {

    private final GetUserByUuidUseCase getUserByUuidUseCase;

    public InternalUserController(GetUserByUuidUseCase getUserByUuidUseCase) {
        this.getUserByUuidUseCase = getUserByUuidUseCase;
    }

    @GetMapping("/users/by-uuid/{uuid}")
    public ResponseEntity<UserResponse> getUserByUuid(@PathVariable("uuid") UUID uuid) {
        return getUserByUuidUseCase.execute(uuid)
                .map(this::toUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUserUuid() != null ? user.getUserUuid().toString() : null,
                user.getUsername(),
                user.getEmail(),
                user.isEnabled()
        );
    }
}
