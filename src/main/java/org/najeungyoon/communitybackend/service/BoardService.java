package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.domain.app.Board;
import org.najeungyoon.communitybackend.model.board.BoardResponseDTO;
import org.najeungyoon.communitybackend.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    /*
        모든 커뮤니티보드를 불러오는 API
        제일 기본 index 페이지에 쓸거에요.
    */
    @Transactional(readOnly = true)
    public List<BoardResponseDTO> findAllBoards(){
        List<Board> allBoards = boardRepository.findAll();

        return allBoards.stream().map(
                board->{
                    return BoardResponseDTO.from(board);
                }
        ).toList();
    }


    /*
        url을 기준으로 특정 커뮤니티 호출하는 API
    */
    @Transactional(readOnly = true)
    public BoardResponseDTO getBoardByUrlPath(String urlPath){

        Board board = boardRepository.findByUrlPath(urlPath)
                .orElseThrow(()->new IllegalArgumentException(urlPath+"커뮤니티는 없습니다."));

        return BoardResponseDTO.from(board);
    }

}
