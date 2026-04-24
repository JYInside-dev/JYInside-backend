package org.najeungyoon.communitybackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.service.PostLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community/board/{urlPath}/post/{id}/likes")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/addLike")
    public ResponseEntity<String> addLike(
            @PathVariable Long id
    ){

        try {
            postLikeService.addLike(id);
            return ResponseEntity.ok("추천 완료되었습니다.");
        }
        /* 추천 주작이나 잘못된 url로 접근했을 때.*/
        catch (IllegalArgumentException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

}
