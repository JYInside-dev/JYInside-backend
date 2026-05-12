package org.najeungyoon.communitybackend.model.comment;


import jakarta.validation.constraints.NotBlank;

/* 프론트엔드에서 댓글을 작성하려고 백엔드로 쏴주는 DTO */
public record CommentCreateRequestDTO(
        String authorName,
        String password,
        @NotBlank(message = "댓글 내용을 입력해 주세요")
        String content,

        Long parentId /* 사용자가 달고 싶은 댓글위치 없으면 null*/
) {
}
