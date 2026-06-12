package org.najeungyoon.communitybackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.service.PostLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community/board/{urlPath}/post/{id}/likes")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PreAuthorize("isAuthenticated() and @communitySecurity.checkNotSuspended(#urlPath)")
    @PostMapping("/addLike")
    public ResponseEntity<Void> addLike(
            @PathVariable Long id,
            @PathVariable String urlPath,
            @AuthenticationPrincipal Long userId
    ){
        postLikeService.addLike(id,userId);
        return ResponseEntity.ok().build();
    }

}
