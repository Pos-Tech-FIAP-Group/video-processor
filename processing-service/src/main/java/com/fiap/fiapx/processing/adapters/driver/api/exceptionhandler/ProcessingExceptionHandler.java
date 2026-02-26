package com.fiap.fiapx.processing.adapters.driver.api.exceptionhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

/**
 * ErrorHandler para exceções do listener RabbitMQ.
 * Garante que erros de validação (IllegalArgumentException) e demais falhas
 * resultem em rejeição sem requeue (mensagem segue para DLQ quando configurado).
 */
@Component
public class ProcessingExceptionHandler implements ErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(ProcessingExceptionHandler.class);

    @Override
    public void handleError(Throwable t) {
        Throwable cause = (t instanceof ListenerExecutionFailedException ex)
                ? ex.getCause()
                : t;

        if (cause instanceof IllegalArgumentException) {
            logger.error("Validation error in message processing, sending to DLQ: {}",
                    cause.getMessage(), cause);
            throw new AmqpRejectAndDontRequeueException("Validation error", cause);
        }

        logger.error("Error processing message, sending to DLQ: {}", cause != null ? cause.getMessage() : t.getMessage(), t);
        throw new AmqpRejectAndDontRequeueException("Processing error", t);
    }
}
