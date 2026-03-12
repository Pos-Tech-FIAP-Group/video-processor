package com.fiap.fiapx.processing.adapters.driven.infra.processing.metadata;

import com.fiap.fiapx.processing.adapters.driven.infra.processing.runner.ProcessRunner;
import com.fiap.fiapx.processing.core.application.ports.VideoMetadataPort;
import com.fiap.fiapx.processing.core.domain.model.VideoDuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Adapter para obter metadados de vídeo usando FFprobe.
 */
@Component
public class FfmpegVideoMetadataAdapter implements VideoMetadataPort {
    
    private final ProcessRunner processRunner;

    public FfmpegVideoMetadataAdapter(ProcessRunner processRunner) {
        this.processRunner = processRunner;
    }
    
    @Override
    public VideoDuration getDuration(Path videoPath) {
        if (videoPath == null || !Files.exists(videoPath)) {
            throw new IllegalArgumentException("Video path must exist: " + videoPath);
        }

        try {
            ProcessRunner.ProcessResult result = processRunner.run(
                    "ffprobe",
                    "-v", "quiet",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    videoPath.toAbsolutePath().toString()
            );

            if (result.exitCode() != 0) {
                throw new IOException("FFprobe exited with code " + result.exitCode());
            }

            String rawOutput = result.stdout() != null ? result.stdout().trim() : "";

            if (rawOutput.isEmpty()) {
                throw new IOException("Could not extract duration from video: " + videoPath);
            }

            String[] outputParts = rawOutput.split("\\s+");
            String durationStr = outputParts[outputParts.length - 1];

            if ("N/A".equalsIgnoreCase(durationStr)) {
                throw new RuntimeException("FFprobe returned N/A. Video metadata is missing duration: " + videoPath);
            }

            double durationSeconds = Double.parseDouble(durationStr);
            return new VideoDuration(durationSeconds);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("FFprobe was interrupted", e);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid duration format from FFprobe", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to get video duration: " + videoPath, e);
        }
    }
}
