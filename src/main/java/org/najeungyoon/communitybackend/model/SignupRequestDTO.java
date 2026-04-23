package org.najeungyoon.communitybackend.model;

public record SignupRequestDTO(
        String username,
        String email,
        String password,
        String nickname
) {
}
