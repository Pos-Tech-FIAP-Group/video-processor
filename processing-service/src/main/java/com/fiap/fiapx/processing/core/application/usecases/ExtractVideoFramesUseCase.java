package com.fiap.fiapx.processing.core.application.usecases;

import com.fiap.fiapx.processing.core.application.ports.VideoFrameExtractorPort;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Use case: extract one frame per second from a video and produce a zip file in the same path.
 */
@Component
public class ExtractVideoFramesUseCase {

    private final VideoFrameExtractorPort videoFrameExtractorPort;

    public ExtractVideoFramesUseCase(VideoFrameExtractorPort videoFrameExtractorPort) {
        this.videoFrameExtractorPort = videoFrameExtractorPort;
    }

    /**
     * Processes the video at the given path and returns the path to the generated zip of frames.
     *
     * @param videoPath path to the video file
     * @return path to the created zip file (in the same directory as the video)
     */
    public Path execute(Path videoPath) {
        return videoFrameExtractorPort.extractFramesPerSecondToZip(videoPath);
    }
}
