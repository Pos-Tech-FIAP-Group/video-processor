package com.fiap.fiapx.processing.adapters.driven.infra.processing.metadata;

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
    
    @Override
    public VideoDuration getDuration(Path videoPath) {
        if (videoPath == null || !Files.exists(videoPath)) {
            throw new IllegalArgumentException("Video path must exist: " + videoPath);
        }
        
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                videoPath.toAbsolutePath().toString()
            );
            
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            String durationStr;
            try (InputStream inputStream = process.getInputStream();
                 Scanner scanner = new Scanner(inputStream)) {
                
                durationStr = scanner.hasNext() ? scanner.next().trim() : null;
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("FFprobe exited with code " + exitCode);
            }
            
            if (durationStr == null || durationStr.isEmpty()) {
                throw new IOException("Could not extract duration from video: " + videoPath);
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
