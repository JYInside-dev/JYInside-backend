package org.najeungyoon.communitybackend.controller;

import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.model.comment.CommentResponseDTO;
import org.najeungyoon.communitybackend.model.post.PostResponseDTO;
import org.najeungyoon.communitybackend.service.MyPageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    /* 내 게시글 몰아보기 */
    @GetMapping("/posts")
    public ResponseEntity<List<PostResponseDTO>> getMyPosts(
            @AuthenticationPrincipal Long userId
    ){
        List<PostResponseDTO> myPosts = myPageService.getMyPosts(userId);

        return ResponseEntity.ok(myPosts);
    }



    /* 내 댓글 몰아보기 */
    @GetMapping("/comments")
    public ResponseEntity<List<CommentResponseDTO>> getMyComments(
            @AuthenticationPrincipal Long userId
    ){
        List<CommentResponseDTO> myComments = myPageService.getMyComments(userId);

        return ResponseEntity.ok(myComments);
    }
}
