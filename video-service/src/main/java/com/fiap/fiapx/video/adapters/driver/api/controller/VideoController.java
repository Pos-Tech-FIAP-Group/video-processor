package com.fiap.fiapx.video.adapters.driver.api.controller;

import com.fiap.fiapx.video.adapters.driven.infra.persistence.repository.PagedResponse;
import com.fiap.fiapx.video.adapters.driver.api.dto.request.CreateVideoRequest;
import com.fiap.fiapx.video.adapters.driver.api.dto.response.CreateVideoResponse;
import com.fiap.fiapx.video.adapters.driver.api.dto.response.VideoResponse;
import com.fiap.fiapx.video.adapters.driver.api.mapper.VideoApiMapper;
import com.fiap.fiapx.video.core.application.usecases.CreateVideoUseCase;
import com.fiap.fiapx.video.core.application.usecases.FindVideoByIdUseCase;
import com.fiap.fiapx.video.core.application.usecases.ListVideosByUserUseCase;
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
    private final FindVideoByIdUseCase findVideoByIdUseCase;
    private final ListVideosByUserUseCase listVideosByUserUseCase;

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

    @GetMapping("/{id}")
    public ResponseEntity<VideoResponse> findById(@PathVariable("id") UUID id) {
        var video = findVideoByIdUseCase.execute(id);
        return ResponseEntity.ok(VideoApiMapper.toResponse(video));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<VideoResponse>> listByUser(
            @RequestParam("userId") UUID userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ){
        var result = listVideosByUserUseCase.execute(userId, page, size);

        var items = result.items().stream()
                .map(VideoApiMapper::toResponse)
                .toList();

        return ResponseEntity.ok(
                new PagedResponse<>(
                        items,
                        result.page(),
                        result.size(),
                        result.totalItems(),
                        result.totalPages()
                )
        );
    }

}