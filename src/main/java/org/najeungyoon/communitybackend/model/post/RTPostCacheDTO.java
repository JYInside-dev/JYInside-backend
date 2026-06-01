package org.najeungyoon.communitybackend.model.post;

import java.time.LocalDateTime;

public record RTPostCacheDTO(
        String urlPath,
        Long postId,
        Long userId,
        String title,
        String authorName,
        Long viewCount,
        LocalDateTime createAt,
        Double score // ZSET에서 가져온 실시간 점수
) {
}
