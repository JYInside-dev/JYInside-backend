package org.najeungyoon.communitybackend.model.login;

public record JwtTokenResponseDTO(
        String grantType,
        String accessToken
) {
}
