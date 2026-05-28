package org.najeungyoon.communitybackend.model.category;

import java.util.List;

public record CategoryReorderDTO(
        List<Long> categoryIdList
) {
}
