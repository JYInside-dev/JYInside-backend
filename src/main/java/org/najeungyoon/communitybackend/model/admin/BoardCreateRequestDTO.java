package org.najeungyoon.communitybackend.model.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.najeungyoon.communitybackend.domain.Board;

public record BoardCreateRequestDTO(
        @NotBlank(message = "커뮤니티 이름은 필수입니다.")
        @Size(min = 2, max = 20, message = "이름은 2~20자 사이여야 합니다.")
        String name,
        @NotBlank(message = "URL경로 설정은 필수입니다.")
        @Pattern(regexp = "^[a-z0-9]+$", message = "URL 경로는 영문 소문자와 숫자만 사용할 수 있습니다.")
        String urlPath,
        String description
) {

    public Board toEntity(){
        return new Board(
                this.name,
                this.urlPath,
                this.description
        );
    }
}
