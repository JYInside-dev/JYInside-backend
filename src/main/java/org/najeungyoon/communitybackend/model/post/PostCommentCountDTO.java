package org.najeungyoon.communitybackend.model.post;

public record PostCommentCountDTO(
        Long postId,
        Long commentCount
) {
}
