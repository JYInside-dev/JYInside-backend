package org.najeungyoon.communitybackend.model;

import org.najeungyoon.communitybackend.domain.Post;
import java.time.LocalDateTime;

public record PostDTO(
        Long id,
        String title,
        String content,
        String authorName,
        LocalDateTime createAt
){

    public static PostDTO from(Post post){
        return new PostDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthorName(),
                post.getCreateAt()
        );
    }
}
