package org.najeungyoon.communitybackend.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostRedisService {

    private final StringRedisTemplate redisTemplate;


    /*
     * 사용자에게 보여줄 현재 총조회수
     *
     * Key: post:view:total
     * Field: 게시글 ID
     * Value: 현재 총조회수
     */
    private static final String TOTAL_VIEW_COUNT_KEY = "post:view:total";

    /*
     * 아직 MySQL에 반영되지 않은 조회수 증가량
     *
     * Key: post:view:pending
     * Field: 게시글 ID
     * Value: DB 미반영 증가량
     */
    private static final String PENDING_VIEW_COUNT_KEY = "post:view:pending";


    /* DB에 미반영증가량과 즉시 표시될 조회수 모두 증가시키는 스크립트 */
    private static final RedisScript<Long> INCREMENT_SCRIPT = RedisScript.of("""
            local postId = ARGV[1]
            local dbViewCount = ARGV[2]
            
            if redis.call('HEXISTS', KEYS[1], postId) == 0 then
                redis.call('HSET', KEYS[1], postId, dbViewCount)
            end
            
            -- DB에 반영해서 늘릴 조회수 +1 
            redis.call('HINCRBY', KEYS[2], postId, 1)
            
            
            -- 표시할 조회수 +1증가 
            return redis.call('HINCRBY', KEYS[1], postId, 1) 
            """, Long.class);


    /* 스크립트 실행 해서 현재 조회수 return */
    public Long incrementAndGet(Long postId, Long dbViewCount) {
        if (postId == null) {
            throw new IllegalArgumentException("게시글 ID가 필요합니다.");
        }
        if (dbViewCount == null) {
            throw new IllegalArgumentException("DB조회수는 0 이상이어야 합니다.");
        }

        Long currentViewCount = redisTemplate.execute(
                INCREMENT_SCRIPT,
                List.of(TOTAL_VIEW_COUNT_KEY, PENDING_VIEW_COUNT_KEY),
                postId.toString(),
                Long.toString(dbViewCount)
        );

        if (currentViewCount == null) {
            throw new IllegalStateException("Redis 조회수 증가 결과가 없습니다.");
        }

        return currentViewCount;
    }





    /* DB에 동기화 스크립트 */
    private static final RedisScript<Long> ACK_PENDING_SCRIPT = RedisScript.of("""
            local postId = ARGV[1]
            local applied = tonumber(ARGV[2]) -- 스크립트가 실행될 시점에 증가할 조회수량
            local current = redis.call('HGET',KEYS[1],postId) -- DB에 조회수를 반영하고 난 후 시점의 증가할 조회수 량.
            
            -- DB에 반영한 후 시점에 증가할 조회수량이 스크립트 실행될 시점 증가량보다 작으면 실행안함.
            if not current or tonumber(current) < applied then
                return -1
            end
            
            --현재 증가량에서 직전에 진행된 증가량 -해줌.
            local remaining = redis.call('HINCRBY',KEYS[1],postId,-applied)
            
            if remaining == 0 then
                redis.call('HDEL',KEYS[1],postId)
            end
            
            return remaining
            """,Long.class);


    public Map<Long,Long> getPendingViewCounts(){
        Map<Object,Object> entries = redisTemplate.opsForHash().entries(PENDING_VIEW_COUNT_KEY);


        Map<Long,Long> result = new HashMap<>();


        /* TODO:
        *   나중에 활성 게시글 수가 매우 많아질경우 병목이 생길 수 있음. */
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            long postId = Long.parseLong(entry.getKey().toString());
            long pendingCount =
                    Long.parseLong(entry.getValue().toString());

            if (pendingCount > 0) {
                result.put(postId, pendingCount);
            }
        }
        return result;
    }



    /* 해당 게시글의 표시용 최신 총조회수 */
    public long getTotalViewCount(Long postId) {
        Object value = redisTemplate.opsForHash()
                .get(TOTAL_VIEW_COUNT_KEY, postId.toString());

        if (value == null) {
            throw new IllegalStateException(
                    "Redis 총조회수가 없습니다. postId=" + postId
            );
        }

        return Long.parseLong(value.toString());
    }



    /* DB 커밋 후, 이번에 처리한 pending 수만 차감 */
    public long acknowledgePending(Long postId, long appliedCount) {
        if (appliedCount <= 0) {
            throw new IllegalArgumentException(
                    "반영 건수는 0보다 커야 합니다."
            );
        }

        Long remaining = redisTemplate.execute(
                ACK_PENDING_SCRIPT,
                List.of(PENDING_VIEW_COUNT_KEY),
                postId.toString(),
                Long.toString(appliedCount)
        );

        if (remaining == null || remaining < 0) {
            throw new IllegalStateException(
                    "Redis pending 차감에 실패했습니다. postId=" + postId
            );
        }

        return remaining;
    }



}
