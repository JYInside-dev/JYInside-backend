package org.najeungyoon.communitybackend.model;

//운영 대시보드용 dto입니다.
public record OpsDTO(
        String activeBackend,
        String frontend,
        String backendBlue,
        String backendGreen,
        String db,
        String nginx,
        String lastDeployResult
) {
}
