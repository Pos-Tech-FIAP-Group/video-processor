package com.fiap.fiapx.processing.adapters.driven.infra.processing.strategy;

import com.fiap.fiapx.processing.adapters.driven.infra.processing.runner.ProcessRunner;
import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Strategy para processamento de vídeos MP4 usando FFmpeg.
 */
@Component
public class FfmpegMp4ProcessingStrategy extends AbstractFfmpegProcessingStrategy {

    public FfmpegMp4ProcessingStrategy(Path zipsDirectoryPath, ProcessRunner processRunner) {
        super(zipsDirectoryPath, processRunner);
    }

    @Override
    protected VideoFormat getSupportedFormat() {
        return VideoFormat.MP4;
    }
}
