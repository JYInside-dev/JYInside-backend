package org.najeungyoon.communitybackend.model.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDTO(
        @NotBlank(message = "아이디를 입력하세요.")
        String username,

        @NotBlank(message = "이메일을 입력하세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "새 비밀번호를 입력하세요.")
        @Size(min = 8, max = 100, message = "새 비밀번호는 8자 이상 입력하세요.")
        String newPassword
) {
}