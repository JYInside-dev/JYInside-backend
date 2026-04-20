package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.domain.Board;
import org.najeungyoon.communitybackend.repository.BoardRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    /*
        모든 커뮤니티보드를 불러오는 API
        제일 기본 index 페이지에 쓸거에요.
    */
    public List<Board> findAllBoards(){
        return boardRepository.findAll();
    }


    /*
        url을 기준으로 특정 커뮤니티 호출하는 API
    */
    public Board getBoardByUrlPath(String urlPath){
        return boardRepository.findByUrlPath(urlPath)
                .orElseThrow(()->new IllegalArgumentException(urlPath+"커뮤니티는 없습니다."));
    }


}
