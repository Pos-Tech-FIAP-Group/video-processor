package com.fiap.fiapx.auth.adapters.driver.api.dto.response;

/**
 * @param userUuid UUID do usuário para uso em outros serviços (ex.: video-service).
 */
public record UserResponse(
        String id,
        String userUuid,
        String username,
        String email,
        boolean enabled
) {
}
