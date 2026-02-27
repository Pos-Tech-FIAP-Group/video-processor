package com.fiap.fiapx.processing.core.application.ports;

import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;

/**
 * Port para obter a strategy de processamento adequada ao formato do vídeo.
 */
public interface VideoProcessingStrategyResolverPort {

    /**
     * Retorna a strategy que suporta o formato informado.
     *
     * @param format formato do vídeo
     * @return strategy de processamento
     * @throws IllegalArgumentException se não existir strategy para o formato
     */
    VideoProcessingStrategyPort getStrategy(VideoFormat format);
}
