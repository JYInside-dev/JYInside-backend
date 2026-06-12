package org.najeungyoon.communitybackend.model.post;

import org.najeungyoon.communitybackend.domain.app.Post;

import java.time.LocalDateTime;

public record PostResponseDTO(
        Long id,
        Long userId, // 비로그인시에는 null
        String title,
        String content,
        String authorName,
        LocalDateTime createAt,
        Integer likeCount,
        Boolean pinned,
        Long categoryId,
        String categoryName,
        Long viewCount,

        String boardUrlPath,

        Integer commentCount
){

    public static PostResponseDTO from(Post post){
        return new PostResponseDTO(
                post.getId(),
                post.getUser() != null ? post.getUser().getId() : null,
                post.getTitle(),
                post.getContent(),
                post.getAuthorName(),
                post.getCreateAt(),
                post.getLikeCount(),
                post.isPinned(),
                post.getCategory() != null ? post.getCategory().getId() : null,
                post.getCategory() != null ? post.getCategory().getName() : null,
                post.getViewCount(),


                post.getBoard().getUrlPath(),
                post.getComments() != null ? post.getComments().size() : 0
        );
    }
}
