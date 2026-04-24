package org.najeungyoon.communitybackend.model.comment;

import org.najeungyoon.communitybackend.domain.Comment;

import java.util.List;


/* 프론트엔드에 띄울 댓글 데이터를 담을 DTO */
public record CommentResponseDTO(
        Long id,
        String content,
        String authorName,
        String mentionedName,
        List<CommentResponseDTO> children
) {


    /* Entity를 DTO로 변환하는 from */
    public static CommentResponseDTO from (Comment comment){

        List<CommentResponseDTO> childDTOs;

        if(comment.getChildren() != null){
            childDTOs = comment.getChildren().stream().map(
                    child ->{
                        return CommentResponseDTO.from(child);
                    }
            ).toList();
        }else {
            childDTOs = List.of(); // null 대신 빈 리스트를 반환하여 프론트엔드의 렌더링 에러 방지
        }


        CommentResponseDTO dto = new CommentResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getAuthorName(),
                comment.getMentionedName(),
                childDTOs
        );
        return dto;
    }
}
