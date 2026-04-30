package org.najeungyoon.communitybackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.model.post.PostResponseDTO;
import org.najeungyoon.communitybackend.service.PostLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/community/board/popular")
@RequiredArgsConstructor
@Slf4j
public class PopularPostController {

    private final PostLikeService postLikeService;


    @GetMapping("/")
    public ResponseEntity<List<PostResponseDTO>> getAllPopularPost(){
        return ResponseEntity.ok(postLikeService.getPopularPost());
    }

    @GetMapping("/top5")
    public ResponseEntity<List<PostResponseDTO>> getTop5PopularPost(){
        return ResponseEntity.ok(postLikeService.getTop5PopularPost());
    }


}
