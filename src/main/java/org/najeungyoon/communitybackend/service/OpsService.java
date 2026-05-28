package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.model.ops.DeployResponseDTO;
import org.najeungyoon.communitybackend.model.ops.OpsDTO;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${ops.server-mode}")
    private String serverMode;

    @Value("${ops.backend-health-url}")
    private String backendHealthUrl;

    @Value("${ops.frontend-health-url}")
    private String frontendHealthUrl;

    @Value("${ops.nginx-health-url}")
    private String nginxHealthUrl;

    @Value("${ops.backend-blue-health-url}")
    private String backendBlueHealthUrl;

    @Value("${ops.backend-green-health-url}")
    private String backendGreenHealthUrl;

    @Value("${ops.frontend-blue-health-url}")
    private String frontendBlueHealthUrl;

    @Value("${ops.frontend-green-health-url}")
    private String frontendGreenHealthUrl;

    @Value("${ops.deploy-request-path}")
    private String deployRequestPath;

    @Value("${ops.deploy-status-path}")
    private String deployStatusPath;


    /* 운영 상태 요약 조회 */
    public OpsDTO getSummary() {
        log.info("운영 상태 요약 조회 service 진입. serverMode={}", serverMode);

        if (isServiceServer()) {
            return getServiceSummary();
        }

        return getTestSummary();
    }

    /* 테스트 서버 상태 요약 */
    private OpsDTO getTestSummary() {
        String backend = checkHealth(backendHealthUrl);
        String frontend = checkHealth(frontendHealthUrl);
        String nginx = checkHealth(nginxHealthUrl);
        String db = checkDatabaseStatus();

        return new OpsDTO(
                "TEST",
                false,

                "-",

                frontend,
                backend,

                "-",
                "-",
                "-",
                "-",

                db,
                nginx,
                "-",

                false,
                "-"
        );
    }

    /* 서비스 서버 상태 요약 */
    private OpsDTO getServiceSummary() {
        String backendBlue = checkHealth(backendBlueHealthUrl);
        String backendGreen = checkHealth(backendGreenHealthUrl);
        String frontendBlue = checkHealth(frontendBlueHealthUrl);
        String frontendGreen = checkHealth(frontendGreenHealthUrl);
        String nginx = checkHealth(nginxHealthUrl);
        String activeBackend = resolveActiveBackend(backendBlue, backendGreen);
        String db = checkDatabaseStatus();
        String deployStatus = readDeployStatus();
        boolean deployInProgress = isDeployInProgress(deployStatus);

        return new OpsDTO(
                "SERVICE",
                true,

                activeBackend,

                "-",
                "-",

                frontendBlue,
                frontendGreen,
                backendBlue,
                backendGreen,

                db,
                nginx,
                deployStatus,

                deployInProgress,
                deployStatus
        );
    }

    /* 무중단 배포 실행 */
    public DeployResponseDTO deploy() {
        log.info("무중단 배포 실행 service 진입. serverMode={}", serverMode);

        if (!isServiceServer()) {
            log.warn("테스트 서버에서 배포 실행 요청이 들어왔습니다.");
            return new DeployResponseDTO(false, "테스트 서버에서는 무중단 배포를 실행할 수 없습니다.");
        }

        try {
            String backendBlue = checkHealth(backendBlueHealthUrl);
            String backendGreen = checkHealth(backendGreenHealthUrl);
            String activeBackend = resolveActiveBackend(backendBlue, backendGreen);
            String targetBackend = resolveDeployTarget(activeBackend);

            log.info("현재 activeBackend={}, 배포 targetBackend={}", activeBackend, targetBackend);

            Path requestPath = Paths.get(deployRequestPath);
            log.info("배포 요청 파일 절대 경로: {}", requestPath.toAbsolutePath());

            Path statusPath = Paths.get(deployStatusPath);
            log.info("배포 상태 파일 절대 경로: {}", statusPath.toAbsolutePath());

            Files.createDirectories(requestPath.getParent());
            log.info("부모 디렉토리 생성/확인 완료: {}", requestPath.getParent());

            Files.createDirectories(statusPath.getParent());
            log.info("부모 디렉토리 생성/확인 완료: {}", statusPath.getParent());

            String now = String.valueOf(System.currentTimeMillis());

            String requestContent = "TARGET=" + targetBackend + System.lineSeparator() + "REQUESTED " + now;
            Files.writeString(requestPath, requestContent);
            log.info("배포 요청 파일 생성 완료. 내용: {}", requestContent);

            String statusContent = "PENDING target=" + targetBackend + " time=" + now;
            Files.writeString(statusPath, statusContent);
            log.info("배포 상태 파일 생성 완료. 내용: {}", statusContent);

            return new DeployResponseDTO(true, "배포 요청 등록됨. target=" + targetBackend);
        } catch (Exception e) {
            log.error("배포 요청 파일 생성 실패", e);
            return new DeployResponseDTO(false, "배포 요청 등록 실패: " + e.getMessage());
        }
    }

    private boolean isServiceServer() {
        return "SERVICE".equalsIgnoreCase(serverMode);
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

    private String resolveDeployTarget(String activeBackend) {
        if ("blue".equalsIgnoreCase(activeBackend)) {
            return "green";
        }

        if ("green".equalsIgnoreCase(activeBackend)) {
            return "blue";
        }

        throw new IllegalArgumentException("현재 active backend를 명확히 판단할 수 없습니다. activeBackend=" + activeBackend);
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

    private String readDeployStatus() {
        try {
            Path path = Paths.get(deployStatusPath);

            if (!Files.exists(path)) {
                return "-";
            }

            return Files.readString(path).trim();
        } catch (Exception e) {
            log.warn("배포 상태 파일 읽기 실패", e);
            return "UNKNOWN";
        }
    }

    private boolean isDeployInProgress(String deployStatus) {
        if (deployStatus == null) return false;

        return deployStatus.startsWith("PENDING") || deployStatus.startsWith("RUNNING");
    }
}
