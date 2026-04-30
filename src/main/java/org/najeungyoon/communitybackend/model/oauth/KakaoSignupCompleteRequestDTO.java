package org.najeungyoon.communitybackend.model.oauth;

public record KakaoSignupCompleteRequestDTO(
        String ticket,
        String username,
        String email,
        String nickname
) {
}
