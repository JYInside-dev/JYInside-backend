package org.najeungyoon.communitybackend.controller;

import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.model.comment.CommentCreateRequestDTO;
import org.najeungyoon.communitybackend.model.comment.CommentResponseDTO;
import org.najeungyoon.communitybackend.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community/board/{urlPath}/post/{postId}/comment")
public class CommentController {

    private final CommentService commentService;


    @PostMapping("/write")
    public ResponseEntity<String > writeComment(
            @PathVariable String urlPath,
            @PathVariable Long postId,
            @RequestBody CommentCreateRequestDTO commentCreateRequestDTO,
            /* 토큰이 검증된 회원이면 ID에 값이 들어가고
            *  아니면 null */
            @AuthenticationPrincipal Long userId
            ){
        commentService.createComment(urlPath,postId,commentCreateRequestDTO,userId);
        return ResponseEntity.status(201).build();
    }


    @GetMapping("/getComments")
    public List<CommentResponseDTO> getComments(
            @PathVariable Long postId,
            @PathVariable String urlPath
    ){
        return commentService.getCommentsByPostId(urlPath,postId);
    }

}
