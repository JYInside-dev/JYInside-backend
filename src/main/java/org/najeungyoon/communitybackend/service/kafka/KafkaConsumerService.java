package org.najeungyoon.communitybackend.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.service.RealTimePopService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ObjectMapper objectMapper;
    private final RealTimePopService realTimePopService;

    /**
     * @KafkaListener: 이 어노테이션이 붙은 메서드는 백그라운드에서 카프카를 계속 주시합니다.
     * topics: 우리가 Fluentd에서 쏘기로 한 토픽 이름
     * groupId: application.yml에 적었던 컨슈머 그룹 이름 (이 그룹이 어디까지 읽었는지 카프카가 기억함)
     */


    @KafkaListener(topics = "post-views", groupId = "community-ranking-group")
    public void consumePostView(String message){
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            // Fluentd가 파싱해서 넘겨준 JSON 구조의 최상단에서 MDC 값들을 바로 꺼냅니다.
            if (jsonNode.has("urlPath") && jsonNode.has("postId")) {
                String urlPath = jsonNode.get("urlPath").asString();
                Long postId = jsonNode.get("postId").asLong();

                // 💡 안전한 로그: 무한 루프 키워드가 없으며, 필요한 데이터만 깔끔하게 출력합니다.
                log.info("🎯 [타겟 발견] 커뮤니티: {}, 게시글 ID: {} 조회수 증가 준비 완료!", urlPath, postId);

                // TODO: Redis에 접속해서 점수를 +1 증가시킨다.
                realTimePopService.incrementPostView(urlPath,postId);
            }
        } catch (Exception e) {
            log.error("💥 [Kafka 파싱 에러] 메시지 형식이 잘못되었습니다.", e);
        }
    }

}
