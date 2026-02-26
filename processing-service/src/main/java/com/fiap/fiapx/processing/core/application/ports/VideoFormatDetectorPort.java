package com.fiap.fiapx.processing.core.application.ports;

import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;

import java.nio.file.Path;

/**
 * Port para detectar o formato de um arquivo de vídeo.
 */
public interface VideoFormatDetectorPort {
    
    /**
     * Detecta o formato do vídeo no caminho especificado.
     * 
     * @param videoPath caminho do arquivo de vídeo
     * @return formato detectado, ou null se não conseguir detectar
     */
    VideoFormat detectFormat(Path videoPath);
}
