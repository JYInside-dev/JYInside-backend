package org.najeungyoon.communitybackend.model.post;

import jakarta.validation.constraints.NotBlank;
import org.najeungyoon.communitybackend.domain.app.Board;
import org.najeungyoon.communitybackend.domain.app.Post;
import org.najeungyoon.communitybackend.domain.app.user.User;


/* 글 작성할 때 입력된 데이터들을 프론트-> 백엔드로 쏴주는 DTO입니다. */
public record PostCreateRequestDTO(

        @NotBlank(message = "제목을 입력해 주세요")
        String title,
        @NotBlank(message = "내용을 입력해 주세요")
        String content,

        /* 로그인한 유저가 입력할때에는 빈칸이 들어오니까 NotBlank를 안붙였어요 */
        String authorName,
        String password

) {

    /* DTO를 Entity로 바로 바꿔주는 함수
    * hashedPassword는 service에서 해쉬된걸로 바로 넣어줘서 조립합니다
    * DTO의 평문 password -> service에서 해쉬한걸로 조립.*/

    /* 얘는 익명용 Post작성하는 메서드
    *  직접 입력받은 authorName, password 사용함 */
    public Post toEntityAnonymous(String hashedPassword, Board board){
        return new Post(
                this.title,
                this.content,
                this.authorName,
                hashedPassword,
                board
        );
    }


    /* 익명아니고 로그인 시 글 작성할 때 사용하는 메서드 */
    public Post toEntityUser(Board board, User user){
        return new Post(
                this.title,
                this.content,
                user,
                board
        );
    }



}
