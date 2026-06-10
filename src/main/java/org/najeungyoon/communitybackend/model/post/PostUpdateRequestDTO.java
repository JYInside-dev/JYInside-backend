package org.najeungyoon.communitybackend.model.post;

import jakarta.validation.constraints.NotBlank;

public record PostUpdateRequestDTO(
        String title,
        String content,
        @NotBlank(message = "비밀번호를 입력해주세요")
        String password) {
}
