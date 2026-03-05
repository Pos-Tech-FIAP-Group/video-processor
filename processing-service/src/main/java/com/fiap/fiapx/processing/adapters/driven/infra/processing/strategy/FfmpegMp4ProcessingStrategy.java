package com.fiap.fiapx.processing.adapters.driven.infra.processing.strategy;

import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Strategy para processamento de vídeos MP4 usando FFmpeg.
 */
@Component
public class FfmpegMp4ProcessingStrategy extends AbstractFfmpegProcessingStrategy {

    public FfmpegMp4ProcessingStrategy(Path zipsDirectoryPath) {
        super(zipsDirectoryPath);
    }

    @Override
    protected VideoFormat getSupportedFormat() {
        return VideoFormat.MP4;
    }
}
