package org.najeungyoon.communitybackend.controller;

import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.model.comment.MyPageCommentResponseDTO;
import org.najeungyoon.communitybackend.model.post.PostResponseDTO;
import org.najeungyoon.communitybackend.model.user.UserPublicProfileResponseDTO;
import org.najeungyoon.communitybackend.service.MyPageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final MyPageService myPageService;



    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserPublicProfileResponseDTO> getProfile(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                myPageService.getPublicProfile(userId)
        );
    }

    @GetMapping("/{userId}/posts")
    public ResponseEntity<List<PostResponseDTO>> getUserPosts(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                myPageService.getPostsByUserId(userId)
        );
    }

    @GetMapping("/{userId}/comments")
    public ResponseEntity<List<MyPageCommentResponseDTO>> getUserComments(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                myPageService.getCommentsByUserId(userId)
        );
    }
}