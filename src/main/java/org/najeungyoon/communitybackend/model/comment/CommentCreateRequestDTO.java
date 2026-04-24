package org.najeungyoon.communitybackend.model.comment;


/* 프론트엔드에서 댓글을 작성하려고 백엔드로 쏴주는 DTO */
public record CommentCreateRequestDTO(
        String authorName,
        String password,
        String content,
        Long parentId /* 사용자가 달고 싶은 댓글위치 없으면 null*/
) {
}
