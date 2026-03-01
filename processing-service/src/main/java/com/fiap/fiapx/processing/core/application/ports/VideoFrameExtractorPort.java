package com.fiap.fiapx.processing.core.application.ports;

import java.nio.file.Path;

/**
 * Port for extracting frames from a video at a configurable interval and packaging them into a zip file.
 */
public interface VideoFrameExtractorPort {

    /**
     * Processes the video at the given path, extracts frames at the given interval,
     * and writes a zip file in the same directory as the video.
     *
     * @param videoPath path to the video file
     * @param frameIntervalSeconds interval in seconds between frames (e.g. 1.0 = one per second, 2.0 = one every 2 seconds)
     * @return path to the created zip file (in the same directory as the video)
     */
    Path extractFramesToZip(Path videoPath, double frameIntervalSeconds);
}
