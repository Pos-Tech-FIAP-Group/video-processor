package com.fiap.fiapx.auth.core.application.ports;

import com.fiap.fiapx.auth.core.domain.model.User;

import java.util.Optional;

/**
 * Porta de geração e validação de JWT. Implementada no adapter driven (security).
 */
public interface TokenProviderPort {

    /**
     * Gera um token JWT para o usuário (ex.: subject = username ou id).
     */
    String generateToken(User user);

    /**
     * Verifica se o token é válido (assinatura e expiração).
     */
    boolean isValid(String token);

    /**
     * Extrai o subject do token (ex.: username ou id do usuário).
     */
    Optional<String> getSubject(String token);
}
