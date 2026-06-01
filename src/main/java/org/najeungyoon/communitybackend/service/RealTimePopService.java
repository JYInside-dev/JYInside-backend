package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RealTimePopService {

    private final StringRedisTemplate redisTemplate;

    // 💡 랭킹을 저장할 Redis Key 이름 (예: 일간 랭킹)
    private static final String RANKING_KEY = "ranking:realTime";

    public void incrementPostView(String urlPath, Long postId){
        String postIdentifier = urlPath + ":" + postId;

        // Redis ZSET의 incrementScore (점수가 없으면 1로 생성, 있으면 +1)
        redisTemplate.opsForZSet().incrementScore(RANKING_KEY, postIdentifier, 1);

        log.info("📈 [Redis 랭킹 업데이트] {} 의 점수가 +1 되었습니다.", postIdentifier);
    }


}
