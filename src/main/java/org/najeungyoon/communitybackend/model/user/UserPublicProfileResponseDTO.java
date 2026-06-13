package org.najeungyoon.communitybackend.model.user;

import org.najeungyoon.communitybackend.domain.app.user.User;

public record UserPublicProfileResponseDTO(
        Long id,
        String nickname
) {
    public static UserPublicProfileResponseDTO from(User user) {
        return new UserPublicProfileResponseDTO(
                user.getId(),
                user.getNickname()
        );
    }
}