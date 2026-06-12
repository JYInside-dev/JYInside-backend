package org.najeungyoon.communitybackend.model.user;

import org.najeungyoon.communitybackend.domain.app.Board;
import org.najeungyoon.communitybackend.domain.app.BoardAdmin;
import org.najeungyoon.communitybackend.domain.app.user.User;

public record BoardAdminUserResponseDTO(
        Long userId,
        String nickname,      // 화면 표시용 유저 이름/닉네임
        Long boardId,
        String boardName,     // 화면 표시용 게시판 이름 (예: 피파 온라인 갤러리)
        String boardUrlPath   // 게시판 식별용 URL 경로 (예: fifa)
) {

    public static BoardAdminUserResponseDTO from(BoardAdmin boardAdmin){
        return new BoardAdminUserResponseDTO(
                boardAdmin.getUser().getId(),
                boardAdmin.getUser().getNickname(),
                boardAdmin.getBoard().getId(),
                boardAdmin.getBoard().getName(),
                boardAdmin.getBoard().getUrlPath()
        );
    }
}
