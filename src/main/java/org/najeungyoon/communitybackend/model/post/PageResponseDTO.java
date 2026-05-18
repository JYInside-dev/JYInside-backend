package org.najeungyoon.communitybackend.model.post;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponseDTO<T>(
        List<T> content,
        int page,
        int size,
        int totalPages,
        long totalElements,
        boolean first,
        boolean last
) {
    public static <T> PageResponseDTO<T> from(Page<T> pageData) {
        return new PageResponseDTO<>(
                pageData.getContent(),
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalPages(),
                pageData.getTotalElements(),
                pageData.isFirst(),
                pageData.isLast()
        );
    }
}
