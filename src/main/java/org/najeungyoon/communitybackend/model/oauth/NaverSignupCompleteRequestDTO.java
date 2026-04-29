package org.najeungyoon.communitybackend.model.oauth;

public record NaverSignupCompleteRequestDTO(
        String ticket,
        String username,
        String email,
        String nickname
) {
}
