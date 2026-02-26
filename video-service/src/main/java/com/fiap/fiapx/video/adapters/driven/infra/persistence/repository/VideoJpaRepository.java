package com.fiap.fiapx.video.adapters.driven.infra.persistence.repository;

import com.fiap.fiapx.video.adapters.driven.infra.persistence.entity.VideoEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VideoJpaRepository extends JpaRepository<VideoEntity, UUID> {
    List<VideoEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    Page<VideoEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}