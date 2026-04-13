package org.najeungyoon.communitybackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.model.PostCreateRequestDTO;
import org.najeungyoon.communitybackend.model.PostDTO;
import org.najeungyoon.communitybackend.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:5173")
@Slf4j
public class PostController {

    private final PostService postService;

    @GetMapping("/getAllPost")
    public List<PostDTO> getAllPost() {
        log.info("react에서 getALlPost요청 들어옴");
        return postService.getAllPost();
    }


    @PostMapping("/write")
    public ResponseEntity<String> writePost(@RequestBody PostCreateRequestDTO dto){
        log.info("글 작성 post 요청 들어옴.");
        postService.createPost(dto);
        return ResponseEntity.ok("성공적으로 게시글 저장 완료됐습니다!");
    }

}
