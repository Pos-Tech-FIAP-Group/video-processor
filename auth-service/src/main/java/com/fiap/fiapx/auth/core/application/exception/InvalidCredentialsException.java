package com.fiap.fiapx.auth.core.application.exception;

/**
 * Lançada quando username/senha são inválidos no login.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
