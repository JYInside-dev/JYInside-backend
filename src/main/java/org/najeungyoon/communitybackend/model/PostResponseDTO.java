package org.najeungyoon.communitybackend.model;

import org.najeungyoon.communitybackend.domain.Post;
import java.time.LocalDateTime;

public record PostResponseDTO(
        Long id,
        String title,
        String content,
        String authorName,
        LocalDateTime createAt
){

    public static PostResponseDTO from(Post post){
        return new PostResponseDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthorName(),
                post.getCreateAt()
        );
    }
}
