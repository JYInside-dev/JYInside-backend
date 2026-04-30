package org.najeungyoon.communitybackend.model.board;

import org.najeungyoon.communitybackend.domain.Board;

public record BoardResponseDTO(
        Long id,
        String name,
        String urlPath,
        String description
) {

    public static BoardResponseDTO from(Board board){
        return new BoardResponseDTO(
                board.getId(),
                board.getName(),
                board.getUrlPath(),
                board.getDescription()
        );
    };

}
