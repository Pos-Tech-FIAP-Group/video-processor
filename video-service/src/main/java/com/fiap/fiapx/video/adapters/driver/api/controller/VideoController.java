package com.fiap.fiapx.video.adapters.driver.api.controller;

import com.fiap.fiapx.video.adapters.driven.infra.persistence.repository.PagedResponse;
import com.fiap.fiapx.video.adapters.driver.api.dto.response.CreateVideoResponse;
import com.fiap.fiapx.video.adapters.driver.api.dto.response.VideoResponse;
import com.fiap.fiapx.video.adapters.driver.api.mapper.VideoApiMapper;
import com.fiap.fiapx.video.adapters.driver.api.files.TempFileStorage;
import com.fiap.fiapx.video.core.application.usecases.CreateVideoUseCase;
import com.fiap.fiapx.video.core.application.usecases.FindVideoByIdUseCase;
import com.fiap.fiapx.video.core.application.usecases.ListVideosByUserUseCase;
import com.fiap.fiapx.video.core.application.usecases.command.CreateVideoCommand;
import com.fiap.fiapx.video.core.domain.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/videos")
public class VideoController {

    private final CreateVideoUseCase createVideoUseCase;
    private final FindVideoByIdUseCase findVideoByIdUseCase;
    private final ListVideosByUserUseCase listVideosByUserUseCase;
    private final TempFileStorage tempFileStorage;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateVideoResponse> create(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestPart("file") MultipartFile file
    ) {
        String storedPath = tempFileStorage.store(userId, file);

        CreateVideoCommand command = new CreateVideoCommand(
                userId,
                file.getOriginalFilename(),
                file.getContentType(),
                storedPath
        );

        createVideoUseCase.execute(command);

        return ResponseEntity.accepted().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<VideoResponse> findById(@PathVariable("id") UUID id) {
        var video = findVideoByIdUseCase.execute(id);
        return ResponseEntity.ok(VideoApiMapper.toResponse(video));
    }

    @GetMapping(value = "/{id}/zip", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> downloadZip(@PathVariable("id") UUID id) {
        Video video = findVideoByIdUseCase.execute(id);
        if (video.getZipPath() == null || video.getZipPath().isBlank()) {
            return ResponseEntity.notFound().build();
        }
        Path path = Paths.get(video.getZipPath());
        if (!Files.isRegularFile(path)) {
            return ResponseEntity.notFound().build();
        }
        String filename = video.getOriginalFilename();
        int lastDot = filename != null ? filename.lastIndexOf('.') : -1;
        String baseName = (lastDot > 0 && filename != null) ? filename.substring(0, lastDot) : (filename != null ? filename : "video");
        String downloadName = baseName + "_frames.zip";
        Resource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadName + "\"")
                .body(resource);
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