package org.najeungyoon.communitybackend.model.ops;

//운영 대시보드용 dto입니다.
public record OpsDTO(
        String serverMode,          // TEST / SERVICE
        boolean deployEnabled,      // 무중단 배포 버튼 표시 여부

        String activeBackend,

        // 테스트 서버용 단일 상태
        String frontend,
        String backend,

        // 서비스 서버용 blue/green 상태
        String frontendBlue,
        String frontendGreen,
        String backendBlue,
        String backendGreen,

        String db,
        String nginx,
        String lastDeployResult,

        boolean deployInProgress,
        String deployStatus
) {
}
