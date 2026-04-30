package org.najeungyoon.communitybackend.model.post;

import jakarta.validation.constraints.NotBlank;

/*
password만 전달하려고 합니다. id는 url로 쏠려고 해요
*/
public record PostDeleteRequestDTO(
        @NotBlank(message = "비밀번호를 입력해주세요")
        String password
) {
}
