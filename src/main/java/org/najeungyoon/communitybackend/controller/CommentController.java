package org.najeungyoon.communitybackend.controller;

import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.model.comment.CommentCreateRequestDTO;
import org.najeungyoon.communitybackend.model.comment.CommentResponseDTO;
import org.najeungyoon.communitybackend.service.CommentService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community/board/{urlPath}/post/{postId}/comment")
public class CommentController {

    private final CommentService commentService;


    @PostMapping("/write")
    public void writeComment(
            @PathVariable String urlPath,
            @PathVariable Long postId,
            @RequestBody CommentCreateRequestDTO commentCreateRequestDTO
            ){
        commentService.createComment(urlPath,postId,commentCreateRequestDTO);
    }


    @GetMapping("/getComments")
    public List<CommentResponseDTO> getComments(
            @PathVariable Long postId,
            @PathVariable String urlPath
    ){
        return commentService.getCommentsByPostId(urlPath,postId);
    }

}
