package com.fiap.fiapx.auth.core.application.exception;

/**
 * Lançada quando se tenta registrar usuário com username ou email já existente.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
