package com.fiap.fiapx.processing.adapters.driven.infra.processing.strategy;

import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import org.springframework.stereotype.Component;

/**
 * Strategy para processamento de vídeos AVI usando FFmpeg.
 */
@Component
public class FfmpegAviProcessingStrategy extends AbstractFfmpegProcessingStrategy {
    
    @Override
    protected VideoFormat getSupportedFormat() {
        return VideoFormat.AVI;
    }
}
