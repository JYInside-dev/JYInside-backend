package org.najeungyoon.communitybackend.model;

public record SignupRequestDTO(
        String email,
        String password,
        String nickname
) {
}
