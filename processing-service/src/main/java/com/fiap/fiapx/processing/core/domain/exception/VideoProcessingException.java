package com.fiap.fiapx.processing.core.domain.exception;

/**
 * Exception para erros no processamento de vídeo (FFmpeg, geração de frames/zip, etc.).
 */
public class VideoProcessingException extends ProcessingException {

    public VideoProcessingException(String message) {
        super(message);
    }

    public VideoProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

