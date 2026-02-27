package com.fiap.fiapx.processing.core.application.ports;

import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import com.fiap.fiapx.processing.core.domain.model.ProcessingResult;
import com.fiap.fiapx.processing.core.domain.model.VideoProcessingRequest;

/**
 * Port para estratégias de processamento de vídeo.
 * Cada implementação suporta um ou mais formatos de vídeo.
 */
public interface VideoProcessingStrategyPort {
    
    /**
     * Processa o vídeo conforme a requisição.
     * 
     * @param request requisição de processamento
     * @return resultado do processamento com path do zip gerado
     */
    ProcessingResult processVideo(VideoProcessingRequest request);
    
    /**
     * Verifica se esta strategy suporta o formato especificado.
     * 
     * @param format formato do vídeo
     * @return true se suporta, false caso contrário
     */
    boolean supports(VideoFormat format);
}
