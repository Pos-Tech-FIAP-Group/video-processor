package com.fiap.fiapx.processing.core.domain.exception;

/**
 * Exception para erros relacionados à obtenção de metadados de vídeo (FFprobe).
 */
public class VideoMetadataException extends ProcessingException {

    public VideoMetadataException(String message) {
        super(message);
    }

    public VideoMetadataException(String message, Throwable cause) {
        super(message, cause);
    }
}

