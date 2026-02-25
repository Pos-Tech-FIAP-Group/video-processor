package com.fiap.fiapx.video.adapters.driver.api.controller;

import com.fiap.fiapx.video.adapters.driver.api.dto.request.CreateVideoRequest;
import com.fiap.fiapx.video.adapters.driver.api.dto.response.CreateVideoResponse;
import com.fiap.fiapx.video.core.application.usecases.CreateVideoUseCase;
import com.fiap.fiapx.video.core.application.usecases.command.CreateVideoCommand;
import com.fiap.fiapx.video.core.domain.model.Video;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/videos")
public class VideoController {

    private final CreateVideoUseCase createVideoUseCase;

    @PostMapping
    public ResponseEntity<CreateVideoResponse> create(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateVideoRequest request
    ) {
        CreateVideoCommand command = new CreateVideoCommand(
                userId,
                request.originalFilename(),
                request.contentType(),
                request.videoPath()
        );

        Video saved = createVideoUseCase.execute(command);

        CreateVideoResponse response = new CreateVideoResponse(
                saved.getId(),
                saved.getStatus(),
                saved.getCreatedAt()
        );

        return ResponseEntity
                .created(URI.create("/api/videos/" + saved.getId()))
                .body(response);
    }
}