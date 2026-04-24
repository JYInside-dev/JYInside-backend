package org.najeungyoon.communitybackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.model.ops.DeployResponseDTO;
import org.najeungyoon.communitybackend.model.ops.OpsDTO;
import org.najeungyoon.communitybackend.service.OpsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ops")
@RequiredArgsConstructor
@Slf4j
public class OpsController {
    private final OpsService opsService;

    /* 운영 상태 요약 조회 */
    @GetMapping("/summary")
    public ResponseEntity<OpsDTO> getSummary() {
        log.info("운영 상태 summary 요청 들어옴");
        OpsDTO response = opsService.getSummary();
        return ResponseEntity.ok(response);
    }

    /* 무중단 배포 실행 */
    @PostMapping("/deploy")
    public ResponseEntity<DeployResponseDTO> deploy() {
        log.info("무중단 배포 실행 요청 들어옴");
        DeployResponseDTO response = opsService.deploy();
        return ResponseEntity.ok(response);
    }
}
