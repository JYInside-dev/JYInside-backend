package org.najeungyoon.communitybackend.model.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequestDTO(
        @NotBlank(message = "현재 비밀번호를 입력하세요.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력하세요.")
        @Size(min = 8, max = 100, message = "새 비밀번호는 8자 이상 입력하세요.")
        String newPassword
) {
}