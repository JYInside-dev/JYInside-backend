package org.najeungyoon.communitybackend.model.board;

import org.najeungyoon.communitybackend.domain.app.Board;

public record BoardNavResponseDTO(
        Long id,
        String name,
        String urlPath
) {
    public static BoardNavResponseDTO from(Board board){
        return new BoardNavResponseDTO(
                board.getId(),
                board.getName(),
                board.getUrlPath()
        );
    }
}
