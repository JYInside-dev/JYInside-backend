package org.najeungyoon.communitybackend.controller;

import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.model.PasswordVerifyRequestDTO;
import org.najeungyoon.communitybackend.model.comment.CommentCreateRequestDTO;
import org.najeungyoon.communitybackend.model.comment.CommentDeleteRequestDTO;
import org.najeungyoon.communitybackend.model.comment.CommentResponseDTO;
import org.najeungyoon.communitybackend.model.comment.CommentUpdateRequestDTO;
import org.najeungyoon.communitybackend.model.post.PageResponseDTO;
import org.najeungyoon.communitybackend.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community/board/{urlPath}/post/{postId}/comment")
public class CommentController {

    private final CommentService commentService;


    @PreAuthorize("isAnonymous() or (isAuthenticated() and @communitySecurity.checkNotSuspended(#urlPath))")
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
        return ResponseEntity.status(201).body("댓글 작성 완료");
    }


    /* 댓글 페이지로 불러오기 */
    @GetMapping("/getComments")
    public ResponseEntity<PageResponseDTO<CommentResponseDTO>> getComments(
            @PathVariable Long postId,
            @PathVariable String urlPath,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                commentService.getCommentsByPostId(urlPath, postId, page, size)
        );
    }


    /* 댓글 삭제하기 */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long postId,
            @PathVariable String urlPath,
            @PathVariable Long commentId,
            @RequestBody(required = false) CommentDeleteRequestDTO deleteRequestDTO,
            @AuthenticationPrincipal Long userId
            ){
        commentService.deleteComment(postId, urlPath, commentId, userId,deleteRequestDTO);
        return ResponseEntity.ok("댓글 삭제 완료");
    }

    /* 댓글 비밀번호 검증 */
    @PostMapping(("/{commentId}/verify-password"))
    public ResponseEntity<Void> verifyCommentPassword(
            @PathVariable String urlPath,
            @PathVariable Long commentId,
            @RequestBody PasswordVerifyRequestDTO dto
    ) {
        // 서비스에 만들어둔 verifyPassword 호출
        commentService.verifyPassword(dto.password(), commentId);
        return ResponseEntity.ok().build();
    }


    /* 댓글 수정하기 */
    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable String urlPath,
            @PathVariable Long commentId,
            @PathVariable Long postId,
            @RequestBody CommentUpdateRequestDTO dto,
            @AuthenticationPrincipal Long userId
    ) {

        commentService.updateComment(postId, urlPath, commentId, userId, dto);
        return ResponseEntity.ok().build(); // 수정 성공 시 200 OK 반환
    }

}
