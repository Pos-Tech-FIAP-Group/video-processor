package com.fiap.fiapx.video.core.application.exceptions;

import java.util.UUID;

public class VideoNotFoundException extends RuntimeException {

    public VideoNotFoundException(UUID id) {
        super("Vídeo não encontrado para o id: " + id);
    }
}