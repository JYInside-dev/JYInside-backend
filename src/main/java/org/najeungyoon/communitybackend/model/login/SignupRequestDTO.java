package org.najeungyoon.communitybackend.model.login;

public record SignupRequestDTO(
        String username,
        String email,
        String password,
        String nickname
) {
}
