package com.fiap.fiapx.video.adapters.driven.infra.persistence.adapter;

import com.fiap.fiapx.video.adapters.driven.infra.persistence.entity.VideoEntity;
import com.fiap.fiapx.video.adapters.driven.infra.persistence.mapper.VideoMapper;
import com.fiap.fiapx.video.adapters.driven.infra.persistence.repository.VideoJpaRepository;
import com.fiap.fiapx.video.core.application.common.PageResult;
import com.fiap.fiapx.video.core.application.ports.VideoRepositoryPort;
import com.fiap.fiapx.video.core.domain.model.Video;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VideoPersistenceAdapter implements VideoRepositoryPort {

    private final VideoJpaRepository repository;

    @Override
    public Video save(Video video) {
        VideoEntity saved = repository.save(VideoMapper.toEntity(video));
        return VideoMapper.toDomain(saved);
    }

    @Override
    public Optional<Video> findById(UUID id) {
        return repository.findById(id).map(VideoMapper::toDomain);
    }

    @Override
    public PageResult<Video> findByUserId(UUID userId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        var resultPage = repository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<Video> items = resultPage.getContent().stream()
                .map(VideoMapper::toDomain)
                .toList();

        return new PageResult<>(
                items,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages()
        );
    }
}