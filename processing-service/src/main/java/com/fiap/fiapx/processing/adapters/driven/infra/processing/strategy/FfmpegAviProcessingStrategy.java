package com.fiap.fiapx.processing.adapters.driven.infra.processing.strategy;

import com.fiap.fiapx.processing.adapters.driven.infra.processing.runner.ProcessRunner;
import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Strategy para processamento de vídeos AVI usando FFmpeg.
 */
@Component
public class FfmpegAviProcessingStrategy extends AbstractFfmpegProcessingStrategy {

    public FfmpegAviProcessingStrategy(Path zipsDirectoryPath, ProcessRunner processRunner) {
        super(zipsDirectoryPath, processRunner);
    }

    @Override
    protected VideoFormat getSupportedFormat() {
        return VideoFormat.AVI;
    }
}
