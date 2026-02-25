package com.fiap.fiapx.video.adapters.driven.infra.persistence.adapter;

import com.fiap.fiapx.video.adapters.driven.infra.persistence.entity.VideoEntity;
import com.fiap.fiapx.video.adapters.driven.infra.persistence.mapper.VideoMapper;
import com.fiap.fiapx.video.adapters.driven.infra.persistence.repository.VideoJpaRepository;
import com.fiap.fiapx.video.core.application.ports.VideoRepositoryPort;
import com.fiap.fiapx.video.core.domain.model.Video;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

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
    public List<Video> findByUserId(UUID userId) {
        return repository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(VideoMapper::toDomain)
                .collect(toList());
    }
}