package org.najeungyoon.communitybackend.model.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(

        @NotBlank(message = "ID를 입력해주세요")
        String username, // id

        @NotBlank(message = "비밀번호를 입력해주세요")
        String password // password
) {
}
