package org.najeungyoon.communitybackend.service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerService {

    /**
     * @KafkaListener: 이 어노테이션이 붙은 메서드는 백그라운드에서 카프카를 계속 주시합니다.
     * topics: 우리가 Fluentd에서 쏘기로 한 토픽 이름
     * groupId: application.yml에 적었던 컨슈머 그룹 이름 (이 그룹이 어디까지 읽었는지 카프카가 기억함)
     */
    @KafkaListener(topics = "post-views", groupId = "community-ranking-group")
    public void consumePostView(String message){
        log.info("[Kafka 낚아채기 성공] 전달받은 메시지: {}",message);

    }

}
