package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.model.DeployResponseDTO;
import org.najeungyoon.communitybackend.model.OpsDTO;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpsService {
    /* 운영 상태 요약 조회 */
    public OpsDTO getSummary() {
        log.info("운영 상태 요약 조회 service 진입");

        // 지금은 임시 하드코딩
        return new OpsDTO(
                "blue",
                "UP",
                "UP",
                "DOWN",
                "UP",
                "UP",
                "SUCCESS"
        );
    }

    /* 무중단 배포 실행 */
    public DeployResponseDTO deploy() {
        log.info("무중단 배포 실행 service 진입");

        // 지금은 임시 응답 반환
        return new DeployResponseDTO(
                true,
                "무중단 배포 요청이 실행되었습니다."
        );
    }
}
