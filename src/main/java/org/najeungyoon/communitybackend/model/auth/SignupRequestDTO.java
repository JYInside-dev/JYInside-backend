package org.najeungyoon.communitybackend.model.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequestDTO(
        @NotBlank(message = "사용할 ID를 입력해 주세요")
        String username, // id

        @NotBlank(message = "사용할 이메일을 입력해 주세요")
        @Email(message = "이메일 형식에 맞게 입력해주세요")
        String email,


        @NotBlank(message = "사용할 비밀번호를 입력해 주세요")
        @Size(min = 8, message = "비밀번호는 최소 8자리 이상이어야 합니다.")
        String password,

        @NotBlank(message = "사용할 닉네임을 입력해 주세요")
        String nickname
) {
}
