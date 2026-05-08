package org.najeungyoon.communitybackend.model.comment;

import org.najeungyoon.communitybackend.domain.app.Comment;

import java.time.LocalDateTime;
import java.util.Locale;

public record MyPageCommentResponseDTO(
        Long id,
        String content,
        String postTitle,
        String boardUrlPath,
        Long postId
/*
         TODO
        comment엔티티에 작성일 표시
        LocalDateTime creatAt
*/
) {


    /* TODO:
    *   fetch join이나 다른방법으로 N+1 최적화 */
    public static MyPageCommentResponseDTO from (Comment comment){
        return new MyPageCommentResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getPost().getTitle(),
                comment.getPost().getBoard().getUrlPath(),
                comment.getPost().getId()
        );
    }
}
