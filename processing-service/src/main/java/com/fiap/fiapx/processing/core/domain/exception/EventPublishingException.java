package com.fiap.fiapx.processing.core.domain.exception;

/**
 * Exception para erros ao publicar eventos de processamento (RabbitMQ).
 */
public class EventPublishingException extends ProcessingException {

    public EventPublishingException(String message) {
        super(message);
    }

    public EventPublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}

