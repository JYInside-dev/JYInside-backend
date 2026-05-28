package org.najeungyoon.communitybackend.model.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryStatusChangeDTO(

        @NotNull(message = "변경할 상태값이 필요합니다.")
        boolean active
) {
}
