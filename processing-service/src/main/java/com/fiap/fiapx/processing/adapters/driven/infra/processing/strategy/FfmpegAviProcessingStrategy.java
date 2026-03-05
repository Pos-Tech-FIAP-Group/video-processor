package com.fiap.fiapx.processing.adapters.driven.infra.processing.strategy;

import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Strategy para processamento de vídeos AVI usando FFmpeg.
 */
@Component
public class FfmpegAviProcessingStrategy extends AbstractFfmpegProcessingStrategy {

    public FfmpegAviProcessingStrategy(Path zipsDirectoryPath) {
        super(zipsDirectoryPath);
    }

    @Override
    protected VideoFormat getSupportedFormat() {
        return VideoFormat.AVI;
    }
}
