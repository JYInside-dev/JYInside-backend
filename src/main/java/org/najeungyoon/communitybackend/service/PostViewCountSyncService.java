package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.repository.PostRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostViewCountSyncService {

    private final PostRedisService postRedisService;
    private final PostRepository postRepository;

    /* 5초 */
    @Scheduled(fixedDelay = 5000)
    public void syncViewCounts(){
        Map<Long,Long> pendingSnapshot = postRedisService.getPendingViewCounts();


        /* 조회가 없으면 종료*/
        if(pendingSnapshot.isEmpty()){
            return;
        }

        for (Map.Entry<Long,Long> entry : pendingSnapshot.entrySet()){
            Long postId = entry.getKey();
            Long pendingCount = entry.getValue();

            try {
                // 1. Redis의 최신 표시용 총조회수 확인
                long redisTotal =
                        postRedisService.getTotalViewCount(postId);

                // 2. MySQL 갱신 및 커밋
                postRepository.syncViewCount(postId, redisTotal);

                // 3. DB 작업이 끝난 뒤 이번 pending만 차감
                postRedisService.acknowledgePending(
                        postId,
                        pendingCount
                );

                log.debug(
                        "조회수 동기화 완료. postId={}, total={}, pending={}",
                        postId,
                        redisTotal,
                        pendingCount
                );
            } catch (Exception e) {
                // 실패한 게시글의 pending은 남겨 다음 실행에서 재시도
                log.error(
                        "조회수 동기화 실패. postId={}",
                        postId,
                        e
                );
            }
        }



    }

}
