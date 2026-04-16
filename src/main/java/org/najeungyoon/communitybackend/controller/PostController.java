package org.najeungyoon.communitybackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.model.PostCreateRequestDTO;
import org.najeungyoon.communitybackend.model.PostDTO;
import org.najeungyoon.communitybackend.model.PostDeleteRequestDTO;
import org.najeungyoon.communitybackend.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    /* 모든 글 보기 ! 기본경로 입니다. */
    @GetMapping("/getAllPost")
    public List<PostDTO> getAllPost() {
        log.info("react에서 getALlPost요청 들어옴");
        return postService.getAllPost();
    }


    /* 글 작성 */
    @PostMapping("/write")
    public ResponseEntity<String> writePost(@RequestBody PostCreateRequestDTO dto){
        log.info("글 작성 post 요청 들어옴.");
        postService.createPost(dto);
        return ResponseEntity.ok("성공적으로 게시글 저장 완료됐습니다!");
    }


    
    /* 클릭했을 때 글 상세 페이지로 이동 */
    @GetMapping("/post/{id}")
    public ResponseEntity<PostDTO> getPost(@PathVariable Long id){
        log.info(id+"번 글 상세페이지 요청 들어옴.");
        PostDTO dto = postService.getPostById(id);
        return ResponseEntity.ok(dto);
    }

    /* 해당 글 삭제 */
    @PostMapping("/post/{id}/delete")
    public ResponseEntity<String> deletePost(
            @PathVariable Long id,
            @RequestBody PostDeleteRequestDTO requestDTO){
        log.info(id+"번 글 삭제 post 요청 들어옴");

        boolean isDeleted = postService.deletePost(id, requestDTO);
        if(isDeleted){
            return ResponseEntity.ok("삭제되었습니다.");
        }else {
            return ResponseEntity.status(403).body("비밀번호가 일치하지 않습니다.");
        }
    }
}
