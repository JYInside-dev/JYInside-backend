package org.najeungyoon.communitybackend.model.oauth;

// 추가 정보 입력 페이지에서 구글 이메일/이름을 미리 보여주기 위한 응답
public record GoogleSignupInfoResponseDTO(
        String email,
        String name,
        String pictureUrl
) {
}
