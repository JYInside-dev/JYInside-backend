package org.najeungyoon.communitybackend.model.comment;

public record CommentUpdateRequestDTO(
        String password,
        String content
) {
}
