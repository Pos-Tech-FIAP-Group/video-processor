package com.fiap.fiapx.auth.adapters.driver.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String token,
        String type,
        long expiresIn,
        String username
) {
    public static AuthResponse of(String token, long expiresInMs, String username) {
        return new AuthResponse(token, "Bearer", expiresInMs, username);
    }
}
