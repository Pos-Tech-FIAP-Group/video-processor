package com.fiap.fiapx.video.adapters.driven.infra.persistence.repository;

public record PagedResponse<T>(
        java.util.List<T> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {}