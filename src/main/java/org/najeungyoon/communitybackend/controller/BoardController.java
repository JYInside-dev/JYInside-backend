package org.najeungyoon.communitybackend.controller;


import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.model.board.BoardNavResponseDTO;
import org.najeungyoon.communitybackend.model.board.BoardResponseDTO;
import org.najeungyoon.communitybackend.service.BoardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community/board")
public class BoardController {

    private final BoardService boardService;


    /* 인덱스 페이지에 보여줄 모든 커뮤니티보드 불러오는 요청*/
    @GetMapping("/getAllBoards")
    public ResponseEntity<List<BoardResponseDTO>> getAllBoards(){
        return ResponseEntity.ok(boardService.findAllBoards());
    }

    /* url인 커뮤니티(Board) 정보를 불러오는 요청 */
    @GetMapping("/{urlPath}")
    public ResponseEntity<BoardResponseDTO> getBoardByUrlPath(
            @PathVariable String urlPath
    ){
        BoardResponseDTO dto = boardService.getBoardByUrlPath(urlPath);
        return ResponseEntity.ok(dto);
    }


    /* 게시판 네비게이션 바 정보 요청 */
    @GetMapping("/{urlPath}/nav")
    public ResponseEntity<BoardNavResponseDTO> getBoardNavByUrlPath(
            @PathVariable String urlPath){
        BoardNavResponseDTO dto = boardService.getBoardNavInfo(urlPath);
        return ResponseEntity.ok(dto);
    }

}
