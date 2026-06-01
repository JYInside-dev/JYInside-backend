package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.domain.app.Post;
import org.najeungyoon.communitybackend.model.post.RTPostCacheDTO;
import org.najeungyoon.communitybackend.repository.PostRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RealTimePopService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final PostRepository postRepository;

    // 랭킹을 저장할 Redis Key 이름
    private static final String RANKING_KEY = "ranking:realTime";
    private static final String CACHE_KEY_PREFIX = "post:detail:";

    /* 카프카가 캐치하면 실행할 메서드
    *  해당 게시글에 점수 +1 */
    public void incrementPostView(String urlPath, Long postId){
        String postIdentifier = urlPath + ":" + postId;

        // Redis ZSET의 incrementScore (점수가 없으면 1로 생성, 있으면 +1)
        redisTemplate.opsForZSet().incrementScore(RANKING_KEY, postIdentifier, 1);

        log.info("📈 [Redis 랭킹 업데이트] {} 의 점수가 +1 되었습니다.", postIdentifier);
    }


    /* redis 에서 점수순으로 가져오는 메서드 */
    public List<RTPostCacheDTO> getTopRealTimePosts(int limit){

        // 1. ZSET에서 점수가 가장 높은 순(내림차순)으로 0등부터 limit-1등까지 가져옴
        Set<ZSetOperations.TypedTuple<String>> topPosts =
                redisTemplate.opsForZSet().reverseRangeWithScores(RANKING_KEY, 0, limit - 1);

        if (topPosts == null || topPosts.isEmpty()) {
            return Collections.emptyList();
        }

        List<RTPostCacheDTO> result = new ArrayList<>();


        /* 점수순으로 가져온 글들의 세부 정보 캐시 히트/미스 처리
        *   캐시에 없음 DB I/O 있으면 바로 가져오기 */
        for (ZSetOperations.TypedTuple<String> tuple : topPosts) {
            String identifier = tuple.getValue(); // "test:2"
            Double score = tuple.getScore();

            String[] parts = identifier.split(":");
            String urlPath = parts[0];
            Long postId = Long.parseLong(parts[1]);

            String cacheKey = CACHE_KEY_PREFIX + identifier;

            try {
                // 2. Redis에서 상세 정보 캐시 조회 (Look-Aside)
                String cachedData = redisTemplate.opsForValue().get(cacheKey);

                if (cachedData != null) {
                    // Cache Hit: DB 접근 없이 Redis 데이터 바로 사용
                    RTPostCacheDTO cachedDto = objectMapper.readValue(cachedData, RTPostCacheDTO.class);
                    // ZSET의 최신 점수로 갱신하여 결과 리스트에 추가
                    result.add(new RTPostCacheDTO(urlPath, postId, cachedDto.userId(),
                            cachedDto.title(), cachedDto.authorName(),
                            cachedDto.viewCount(),cachedDto.createAt(), score));
                } else {
                    // Cache Miss: DB에서 직접 조회 후 캐시에 저장
                    log.info("캐시 미스 발생. DB에서 데이터를 조회합니다. 식별자: {}", identifier);

                    Post post = postRepository.findById(postId)
                            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

                    RTPostCacheDTO newDto = new RTPostCacheDTO(urlPath, postId, post.getUser().getId(),
                            post.getTitle(), post.getAuthorName(), post.getViewCount(),post.getCreateAt(),score);

                    // DB에서 가져온 데이터를 JSON으로 직렬화하여 Redis에 캐싱 (TTL: 1시간 설정)
                    redisTemplate.opsForValue().set(
                            cacheKey,
                            objectMapper.writeValueAsString(newDto),
                            Duration.ofHours(1)
                    );

                    result.add(newDto);
                }
            } catch (Exception e) {
                log.error("Redis 데이터 직렬화/역직렬화 오류", e);
            }
        }
        return result;


    }







}
