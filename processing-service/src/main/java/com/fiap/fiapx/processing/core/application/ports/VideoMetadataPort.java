package com.fiap.fiapx.processing.core.application.ports;

import com.fiap.fiapx.processing.core.domain.model.VideoDuration;

import java.nio.file.Path;

/**
 * Port para obter metadados de um arquivo de vídeo.
 */
public interface VideoMetadataPort {
    
    /**
     * Obtém a duração do vídeo em segundos.
     * 
     * @param videoPath caminho do arquivo de vídeo
     * @return duração do vídeo
     * @throws RuntimeException se não conseguir obter a duração
     */
    VideoDuration getDuration(Path videoPath);
}
