package com.fiap.fiapx.auth.core.application.ports;

/**
 * Porta para codificação e verificação de senha. Implementada no adapter (BCrypt).
 */
public interface PasswordEncoderPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
