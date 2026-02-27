package com.fiap.fiapx.processing.adapters.driven.infra.processing.strategy;

import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import org.springframework.stereotype.Component;

/**
 * Strategy para processamento de vídeos MOV usando FFmpeg.
 */
@Component
public class FfmpegMovProcessingStrategy extends AbstractFfmpegProcessingStrategy {
    
    @Override
    protected VideoFormat getSupportedFormat() {
        return VideoFormat.MOV;
    }
}
