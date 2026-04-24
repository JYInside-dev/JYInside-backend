package org.najeungyoon.communitybackend.model.auth;

public record SignupRequestDTO(
        String username,
        String email,
        String password,
        String nickname
) {
}
