package com.fiap.fiapx.auth.core.application.usecases;

/**
 * Resultado do caso de uso de autenticação: token JWT e dados mínimos para o cliente.
 */
public record AuthResult(String token, String username, long expiresInMs, String userId, String userUuid) {
}
