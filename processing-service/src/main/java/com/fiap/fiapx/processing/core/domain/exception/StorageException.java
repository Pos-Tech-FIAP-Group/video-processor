package com.fiap.fiapx.processing.core.domain.exception;

/**
 * Exception para erros de armazenamento, como falhas de upload para S3.
 */
public class StorageException extends ProcessingException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

