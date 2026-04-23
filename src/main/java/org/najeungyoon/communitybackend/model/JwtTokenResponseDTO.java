package org.najeungyoon.communitybackend.model;

public record JwtTokenResponseDTO(
        String grantType,
        String accessToken
) {
}
