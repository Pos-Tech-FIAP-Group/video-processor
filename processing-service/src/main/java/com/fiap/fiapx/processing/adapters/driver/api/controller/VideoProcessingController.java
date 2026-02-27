package com.fiap.fiapx.processing.adapters.driver.api.controller;

import com.fiap.fiapx.processing.adapters.driver.api.dto.request.ExtractFramesRequest;
import com.fiap.fiapx.processing.adapters.driver.api.dto.response.ExtractFramesResponse;
import com.fiap.fiapx.processing.core.application.usecases.ExtractVideoFramesUseCase;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * REST controller for video processing operations.
 * Video paths in requests are relative to the configured base path (e.g. /data):
 * "video.mp4", "/video.mp4", "video1/video.mp4" all resolve under base path.
 */
@RestController
@RequestMapping("/api/process")
public class VideoProcessingController {

    private final ExtractVideoFramesUseCase extractVideoFramesUseCase;
    private final Path storageBasePath;

    public VideoProcessingController(
            ExtractVideoFramesUseCase extractVideoFramesUseCase,
            @Value("${processing.storage.base-path:/data}") String storageBasePath) {
        this.extractVideoFramesUseCase = extractVideoFramesUseCase;
        this.storageBasePath = Paths.get(storageBasePath).toAbsolutePath().normalize();
    }

    /**
     * Extracts frames from the video at the given path (relative to base path, e.g. /data)
     * with the specified interval in seconds, and creates a zip in the same directory.
     */
    @PostMapping("/extract-frames")
    public ResponseEntity<ExtractFramesResponse> extractFrames(@Valid @RequestBody ExtractFramesRequest request) {
        Path videoPath = resolveVideoPath(request.videoPath());
        double intervalSeconds = request.frameIntervalSecondsOrDefault();
        Path zipPath = extractVideoFramesUseCase.execute(videoPath, intervalSeconds);
        return ResponseEntity.ok(new ExtractFramesResponse(zipPath.toString()));
    }

    /**
     * Resolves the request path relative to the storage base path.
     * Accepts: "video.mp4", "/video.mp4", "video1/video.mp4" (with or without leading slash).
     */
    private Path resolveVideoPath(String videoPathInput) {
        if (videoPathInput == null || videoPathInput.isBlank()) {
            throw new IllegalArgumentException("videoPath is required");
        }
        String normalized = videoPathInput.trim().replaceFirst("^/+", "");
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("videoPath is required");
        }
        Path resolved = storageBasePath.resolve(normalized).normalize();
        if (!resolved.startsWith(storageBasePath)) {
            throw new IllegalArgumentException("videoPath must be under base path: " + storageBasePath);
        }
        return resolved;
    }
}
