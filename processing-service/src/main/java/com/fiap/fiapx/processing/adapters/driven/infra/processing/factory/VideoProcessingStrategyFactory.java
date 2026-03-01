package com.fiap.fiapx.processing.adapters.driven.infra.processing.factory;

import com.fiap.fiapx.processing.core.application.ports.VideoProcessingStrategyPort;
import com.fiap.fiapx.processing.core.application.ports.VideoProcessingStrategyResolverPort;
import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Implementação da port para selecionar a strategy apropriada baseada no formato do vídeo.
 */
@Component
public class VideoProcessingStrategyFactory implements VideoProcessingStrategyResolverPort {
    
    private final List<VideoProcessingStrategyPort> strategies;
    
    public VideoProcessingStrategyFactory(List<VideoProcessingStrategyPort> strategies) {
        this.strategies = strategies;
    }
    
    /**
     * Seleciona a strategy apropriada para o formato especificado.
     * 
     * @param format formato do vídeo
     * @return strategy encontrada
     * @throws IllegalArgumentException se nenhuma strategy suportar o formato
     */
    public VideoProcessingStrategyPort getStrategy(VideoFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("Video format cannot be null");
        }
        
        return strategies.stream()
                .filter(strategy -> strategy.supports(format))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "No strategy found for video format: " + format));
    }
    
    /**
     * Tenta encontrar uma strategy para o formato, retornando Optional.empty() se não encontrar.
     */
    public Optional<VideoProcessingStrategyPort> findStrategy(VideoFormat format) {
        if (format == null) {
            return Optional.empty();
        }
        
        return strategies.stream()
                .filter(strategy -> strategy.supports(format))
                .findFirst();
    }
}
