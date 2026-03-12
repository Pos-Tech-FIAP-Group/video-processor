package com.fiap.fiapx.processing.adapters.driven.infra.processing.detector;

import com.fiap.fiapx.processing.core.application.ports.VideoFormatDetectorPort;
import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Detector de formato de vídeo usando análise de extensão do arquivo.
 * Pode ser estendido para usar FFprobe para detecção mais precisa.
 */
@Component
public class FfmpegVideoFormatDetector implements VideoFormatDetectorPort {
    
    @Override
    public VideoFormat detectFormat(Path videoPath) {
        if (videoPath == null || !Files.exists(videoPath)) {
            return null;
        }
        
        // Primeiro tenta detectar pela extensão
        VideoFormat format = VideoFormat.fromExtension(videoPath.getFileName().toString());
        if (format != null) {
            return format;
        }
        
        // Se não conseguir pela extensão, pode tentar usar FFprobe
        // Por enquanto, retorna null se não conseguir detectar
        return null;
    }
}
