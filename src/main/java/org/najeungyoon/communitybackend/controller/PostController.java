package org.najeungyoon.communitybackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.model.PostCreateRequestDTO;
import org.najeungyoon.communitybackend.model.PostDTO;
import org.najeungyoon.communitybackend.model.PostDeleteRequestDTO;
import org.najeungyoon.communitybackend.service.BoardService;
import org.najeungyoon.communitybackend.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community/{urlPath}")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final BoardService boardService;

    /* 특정 커뮤니티의 모든 글 보기 */
    @GetMapping("/allPost")
    public ResponseEntity<List<PostDTO>> getAllPost(
            @PathVariable String urlPath
    ) {
        List<PostDTO> posts = postService.getAllPostsByBoard(urlPath);

        // 200ok
        return ResponseEntity.ok(posts);
    }


    /* 글 작성 */
    @PostMapping("/write")
    public ResponseEntity<String> writePost(
            @PathVariable String urlPath,
            @RequestBody PostCreateRequestDTO dto
            ){
        log.info("{} 에 글 작성 post 요청 들어옴.",urlPath);
        postService.createPost(urlPath,dto);

        return ResponseEntity.status(201).build();
    }


    /* 클릭했을 때 글 상세 페이지로 이동 */
    @GetMapping("/post/{id}")
    public ResponseEntity<PostDTO> getPost(
            @PathVariable String urlPath,
            @PathVariable Long id){
        log.info("{}커뮤니티 {}번 글 상세페이지 요청 들어옴.",urlPath,id);
        PostDTO dto = postService.getPostById(urlPath,id);
        return ResponseEntity.ok(dto);
    }

    /* 해당 글 삭제 */
    @PostMapping("/post/{id}/delete")
    public ResponseEntity<String> deletePost(
            @PathVariable String urlPath,
            @PathVariable Long id,
            @RequestBody PostDeleteRequestDTO requestDTO){
        log.info("{}커뮤니티 {}번 글 삭제 post 요청 들어옴",urlPath,id);

        boolean isDeleted = postService.deletePost(urlPath, id, requestDTO);
        if(isDeleted){
            return ResponseEntity.ok("삭제되었습니다.");
        }else {
            return ResponseEntity.status(403).body("비밀번호가 일치하지 않습니다.");
        }
    }
}
