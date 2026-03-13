package com.fiap.fiapx.notification.core.domain.model;

/**
 * DTO minimo com dados do usuario obtidos do auth-service (GET /internal/users/by-uuid).
 * Usado para envio de e-mail de falha (destinatario e opcionalmente nome no corpo).
 */
public record UserInfo(String email, String username) {
}
