package org.najeungyoon.communitybackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.model.post.PostResponseDTO;
import org.najeungyoon.communitybackend.model.post.RTPostCacheDTO;
import org.najeungyoon.communitybackend.service.PostLikeService;
import org.najeungyoon.communitybackend.service.RealTimePopService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/community/board/popular")
@RequiredArgsConstructor
@Slf4j
public class PopularPostController {

    private final PostLikeService postLikeService;
    private final RealTimePopService realTimePopService;


    @GetMapping("/")
    public ResponseEntity<List<PostResponseDTO>> getAllPopularPost(){
        return ResponseEntity.ok(postLikeService.getPopularPost());
    }

    @GetMapping("/top5")
    public ResponseEntity<List<RTPostCacheDTO>> top5RealTimePopularPost(){
        return ResponseEntity.ok(realTimePopService.getTopRealTimePosts(5));
    }

    @GetMapping("/realtime-popular")
    public ResponseEntity<List<RTPostCacheDTO>> getRealTimePopularPost(
            @RequestParam(defaultValue = "10") int limit)
    {
        return ResponseEntity.ok(realTimePopService.getTopRealTimePosts(limit));
    }

}
