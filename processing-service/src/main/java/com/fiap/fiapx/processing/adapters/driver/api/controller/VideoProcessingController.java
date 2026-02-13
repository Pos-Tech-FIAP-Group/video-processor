package com.fiap.fiapx.processing.adapters.driver.api.controller;

import com.fiap.fiapx.processing.adapters.driver.api.dto.request.ExtractFramesRequest;
import com.fiap.fiapx.processing.adapters.driver.api.dto.response.ExtractFramesResponse;
import com.fiap.fiapx.processing.core.application.usecases.ExtractVideoFramesUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * REST controller for video processing operations.
 */
@RestController
@RequestMapping("/api/process")
public class VideoProcessingController {

    private final ExtractVideoFramesUseCase extractVideoFramesUseCase;

    public VideoProcessingController(ExtractVideoFramesUseCase extractVideoFramesUseCase) {
        this.extractVideoFramesUseCase = extractVideoFramesUseCase;
    }

    /**
     * Extracts one frame per second from the video at the given path and creates a zip file
     * in the same directory. Returns the path to the created zip.
     */
    @PostMapping("/extract-frames")
    public ResponseEntity<ExtractFramesResponse> extractFrames(@Valid @RequestBody ExtractFramesRequest request) {
        Path videoPath = Paths.get(request.videoPath());
        Path zipPath = extractVideoFramesUseCase.execute(videoPath);
        return ResponseEntity.ok(new ExtractFramesResponse(zipPath.toString()));
    }
}
