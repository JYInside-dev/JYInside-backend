package org.najeungyoon.communitybackend.model.oauth;

public record GoogleSignupCompleteRequestDTO(
        String ticket,
        String username,
        String nickname
) {
}
