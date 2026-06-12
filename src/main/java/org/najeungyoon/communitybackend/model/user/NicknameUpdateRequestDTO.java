package org.najeungyoon.communitybackend.model.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameUpdateRequestDTO(
        @NotBlank(message = "닉네임을 입력하세요.")
        @Size(min = 2, max = 30, message = "닉네임은 30자 이하로 입력하세요.")
        String nickname
) {
}