package org.najeungyoon.communitybackend.model.user;

public record UserMeResponseDTO(
        Long id,
        String username,
        String email,
        String nickname,
        String role,
        String status,
        boolean socialUser
) {
}