package com.fiap.fiapx.processing.core.application.ports;

import java.nio.file.Path;

/**
 * Port for extracting one frame per second from a video and packaging them into a zip file.
 */
public interface VideoFrameExtractorPort {

    /**
     * Processes the video at the given path, extracts one image per second,
     * and writes a zip file in the same directory as the video.
     *
     * @param videoPath path to the video file
     * @return path to the created zip file (in the same directory as the video)
     */
    Path extractFramesPerSecondToZip(Path videoPath);
}
