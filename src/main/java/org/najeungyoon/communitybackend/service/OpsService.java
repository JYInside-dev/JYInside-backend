package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.model.ops.DeployResponseDTO;
import org.najeungyoon.communitybackend.model.ops.OpsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpsService {
    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    /* 운영 상태 요약 조회 */
    public OpsDTO getSummary() {
        log.info("운영 상태 요약 조회 service 진입");

        String backendBlue = checkHealth("http://backend-blue:8080/actuator/health");
        String backendGreen = checkHealth("http://backend-green:8080/actuator/health");
        String frontendBlue = checkHealth("http://frontend-blue");
        String frontendGreen = checkHealth("http://frontend-green");
        String nginx = checkHealth("http://nginx");
        String activeBackend = resolveActiveBackend(backendBlue, backendGreen);
        String db = checkDatabaseStatus();

        return new OpsDTO(
                activeBackend,
                frontendBlue,
                frontendGreen,
                backendBlue,
                backendGreen,
                db,
                nginx,
                "SUCCESS"
        );
    }

    /* 무중단 배포 실행 */
    public DeployResponseDTO deploy() {
        log.info("무중단 배포 실행 service 진입");

        try {
            Path requestPath = Paths.get("/app/ops/deploy-request.txt");
            log.info("요청 파일 절대 경로: {}", requestPath.toAbsolutePath());

            Files.createDirectories(requestPath.getParent());
            log.info("부모 디렉토리 생성/확인 완료: {}", requestPath.getParent());

            String content = "REQUESTED " + System.currentTimeMillis();
            Files.writeString(requestPath, content);
            log.info("파일 쓰기 완료. 내용: {}", content);

            return new DeployResponseDTO(true, "배포 요청 등록됨");
        } catch (Exception e) {
            log.error("배포 요청 파일 생성 실패", e);
            return new DeployResponseDTO(false, "배포 요청 등록 실패: " + e.getMessage());
        }
    }

    private String checkHealth(String url){
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return "UP";
            }
            return "DOWN";
        } catch (Exception e) {
            log.warn("헬스 체크 실패: {}", url);
            return "DOWN";
        }
    }

    private String resolveActiveBackend(String backendBlue, String backendGreen) {
        if ("UP".equals(backendBlue) && "DOWN".equals(backendGreen)) return "blue";
        if ("DOWN".equals(backendBlue) && "UP".equals(backendGreen)) return "green";
        if ("UP".equals(backendBlue) && "UP".equals(backendGreen)) return "both";
        return "unknown";
    }

    private String checkDatabaseStatus() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return (result != null && result == 1) ? "UP" : "DOWN";
        } catch (Exception e) {
            log.warn("DB 상태 체크 실패", e);
            return "DOWN";
        }
    }
}
