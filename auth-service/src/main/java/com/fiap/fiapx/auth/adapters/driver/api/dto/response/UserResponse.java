package com.fiap.fiapx.auth.adapters.driver.api.dto.response;

public record UserResponse(
        String id,
        String username,
        String email,
        boolean enabled
) {
}
