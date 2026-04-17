package org.najeungyoon.communitybackend.model;

import org.najeungyoon.communitybackend.domain.Post;


/* 글 작성할 때 입력된 데이터들을 프론트-> 백엔드로 쏴주는 DTO입니다. */
public record PostCreateRequestDTO(
        String title,
        String content,
        String authorName,
        String password
) {

    /* DTO를 Entity로 바로 바꿔주는 함수
    * hashedPassword는 service에서 해쉬된걸로 바로 넣어줘서 조립합니다
    * DTO의 평문 password -> service에서 해쉬한걸로 조립.*/
    public Post toEntity(String hashedPassword){
        return new Post(
                this.title,
                this.content,
                this.authorName,
                hashedPassword
        );
    }
}
