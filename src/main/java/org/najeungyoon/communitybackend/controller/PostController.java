package org.najeungyoon.communitybackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.model.post.PostCreateRequestDTO;
import org.najeungyoon.communitybackend.model.post.PostResponseDTO;
import org.najeungyoon.communitybackend.model.post.PostDeleteRequestDTO;
import org.najeungyoon.communitybackend.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community/board/{urlPath}")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;


    /* 특정 커뮤니티의 모든 글 보기 */
    @GetMapping("/allPost")
    public ResponseEntity   <List<PostResponseDTO>> getAllPost(
            @PathVariable String urlPath
    ) {
        List<PostResponseDTO> posts = postService.getAllPostsByBoard(urlPath);

        // 200ok
        return ResponseEntity.ok(posts);
    }


    /* 글 작성 */
    @PreAuthorize("hasRole('USER') and @communitySecurity.checkNotSuspended(#urlPath)")
    @PostMapping("/write")
    public ResponseEntity<String> writePost(
            @PathVariable String urlPath,
            @RequestBody PostCreateRequestDTO dto,
            @AuthenticationPrincipal Long userId /* 로그인 했으면 값이 들어가고 아니면 null */
            ){

        log.info("{} 에 글 작성 post 요청 들어옴.",urlPath);

        postService.createPost(urlPath, dto,userId);

        return ResponseEntity.status(201).build();
    }


    /* 클릭했을 때 글 상세 페이지로 이동 */
    @GetMapping("/post/{id}")
    public ResponseEntity<PostResponseDTO> getPost(
            @PathVariable String urlPath,
            @PathVariable Long id){
        log.info("{}커뮤니티 {}번 글 상세페이지 요청 들어옴.",urlPath,id);
        PostResponseDTO dto = postService.getPostById(urlPath,id);
        return ResponseEntity.ok(dto);
    }


    /* 해당 글 삭제 */
    @PostMapping("/post/{id}/delete")
    public ResponseEntity<String> deletePost(
            @PathVariable String urlPath,
            @PathVariable Long id,
            @RequestBody(required = false) PostDeleteRequestDTO requestDTO,
            @AuthenticationPrincipal Long userId){
        log.info("{}커뮤니티 {}번 글 삭제 post 요청 들어옴",urlPath,id);

        postService.deletePost(urlPath, id, requestDTO,userId);

        return ResponseEntity.ok().build();
    }


    /* 글 검색 */
    @GetMapping("/posts/search")
    public ResponseEntity<List<PostResponseDTO>> searchPosts(
            @PathVariable String urlPath,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam String keyword
    ) {
        List<PostResponseDTO> response = postService.searchPosts(urlPath, type, keyword);

        return ResponseEntity.ok(response);
    }


    /* 고정 글 조회 */
    @GetMapping("/posts/pinned")
    public ResponseEntity<List<PostResponseDTO>> getPinnedPosts(
            @PathVariable String urlPath
    ) {
        return ResponseEntity.ok(postService.getPinnedPosts(urlPath));
    }


}
