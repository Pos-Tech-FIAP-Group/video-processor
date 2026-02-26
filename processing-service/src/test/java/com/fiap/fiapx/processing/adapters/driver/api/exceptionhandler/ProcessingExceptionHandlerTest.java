package com.fiap.fiapx.processing.adapters.driver.api.exceptionhandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;

import static org.junit.jupiter.api.Assertions.*;

class ProcessingExceptionHandlerTest {

    private ProcessingExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ProcessingExceptionHandler();
    }

    @Test
    void shouldThrowAmqpRejectAndDontRequeueWhenCauseIsIllegalArgumentException() {
        IllegalArgumentException cause = new IllegalArgumentException("Invalid frame interval");
        ListenerExecutionFailedException exception = new ListenerExecutionFailedException(
                "Listener failed", cause);

        AmqpRejectAndDontRequeueException thrown = assertThrows(
                AmqpRejectAndDontRequeueException.class,
                () -> handler.handleError(exception)
        );

        assertNotNull(thrown.getCause());
        assertSame(cause, thrown.getCause());
        assertTrue(thrown.getMessage().contains("Validation"));
    }

    @Test
    void shouldThrowAmqpRejectAndDontRequeueWhenCauseIsRuntimeException() {
        RuntimeException cause = new RuntimeException("FFmpeg failed");
        ListenerExecutionFailedException exception = new ListenerExecutionFailedException(
                "Listener failed", cause);

        AmqpRejectAndDontRequeueException thrown = assertThrows(
                AmqpRejectAndDontRequeueException.class,
                () -> handler.handleError(exception)
        );

        assertNotNull(thrown.getCause());
        assertSame(exception, thrown.getCause());
        assertTrue(thrown.getMessage().contains("Processing"));
    }

    @Test
    void shouldThrowAmqpRejectAndDontRequeueWhenThrowableIsNotListenerExecutionFailedException() {
        RuntimeException error = new RuntimeException("Unexpected error");

        AmqpRejectAndDontRequeueException thrown = assertThrows(
                AmqpRejectAndDontRequeueException.class,
                () -> handler.handleError(error)
        );

        assertSame(error, thrown.getCause());
        assertTrue(thrown.getMessage().contains("Processing"));
    }
}
