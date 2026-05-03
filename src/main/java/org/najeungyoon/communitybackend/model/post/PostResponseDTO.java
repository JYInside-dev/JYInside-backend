package org.najeungyoon.communitybackend.model.post;

import org.najeungyoon.communitybackend.domain.app.Post;

import java.time.LocalDateTime;

public record PostResponseDTO(
        Long id,
        String title,
        String content,
        String authorName,
        LocalDateTime createAt,
        Integer likeCount,
        /* TODO
        *   n+1 문제 나중에 해결해야함
        *   DTO에서 다른 table을 참조하는게 바람직하지 x*/
        String boardUrlPath
){

    public static PostResponseDTO from(Post post){
        return new PostResponseDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthorName(),
                post.getCreateAt(),
                post.getLikeCount(),

                /* TODO
                 *   n+1 문제 나중에 해결해야함
                 *   DTO에서 다른 table을 참조하는게 바람직하지 x*/
                post.getBoard().getUrlPath()
        );
    }
}
