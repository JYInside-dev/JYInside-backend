package org.najeungyoon.communitybackend.model.deploy;

// 배포 버튼 응답용 dto입니다.
public record DeployResponseDTO(
        boolean success,
        String message
) {
}
