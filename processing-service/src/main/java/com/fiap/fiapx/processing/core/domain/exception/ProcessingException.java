package com.fiap.fiapx.processing.core.domain.exception;

/**
 * Exception base de domínio para erros do processamento de vídeo.
 */
public class ProcessingException extends RuntimeException {

    public ProcessingException(String message) {
        super(message);
    }

    public ProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

