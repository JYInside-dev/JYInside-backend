package org.najeungyoon.communitybackend.model.user;

import org.najeungyoon.communitybackend.domain.app.user.UserCommunitySuspension;

import java.time.LocalDateTime;

public record SuspendedUserResponseDTO(
        Long userId,
        String email,
        String nickname,
        LocalDateTime suspendedAt,
        LocalDateTime suspendUntil
) {

    public static SuspendedUserResponseDTO from(UserCommunitySuspension suspension) {
        return new SuspendedUserResponseDTO(
                suspension.getUser().getId(),
                suspension.getUser().getEmail(),
                suspension.getUser().getNickname(),
                suspension.getLastSuspendedAt(), // 정지된 날짜 (Auditing 필드 가정)
                suspension.getSuspendUntil()
        );
    }
}
