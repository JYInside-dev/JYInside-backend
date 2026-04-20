package org.najeungyoon.communitybackend.controller;


import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.domain.Board;
import org.najeungyoon.communitybackend.service.BoardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
public class BoardController {

    private final BoardService boardService;


    /* 인덱스 페이지에 보여줄 모든 커뮤니티보드 불러오는 요청*/
    @GetMapping("/getAllBoards")
    public List<Board> getAllBoards(){
        return boardService.findAllBoards();
    }


    /* url인 커뮤니티(Board) 정보를 불러오는 요청 */
    @GetMapping("/{urlPath}/board")
    public Board getBoardByUrlPath(
            @PathVariable String urlPath
    ){
        return boardService.getBoardByUrlPath(urlPath);
    }
}
