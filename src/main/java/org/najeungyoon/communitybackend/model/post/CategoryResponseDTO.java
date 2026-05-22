package org.najeungyoon.communitybackend.model.post;

import org.najeungyoon.communitybackend.domain.app.Category;

public record CategoryResponseDTO(
        Long id,
        String name,
        int displayOrder,
        boolean active
) {
    public static CategoryResponseDTO from(Category category) {
        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getDisplayOrder(),
                category.isActive()
        );
    }
}
