package org.najeungyoon.communitybackend.controller.admin;


import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.model.comment.AdminCommentDeleteRequestDTO;
import org.najeungyoon.communitybackend.service.admin.CommunityAdminService;
import org.springframework.boot.actuate.web.exchanges.HttpExchange;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/community/board/{urlPath}")
@RequiredArgsConstructor
public class CommunityAdminController {

    private final CommunityAdminService communityAdminService;


    @DeleteMapping("/post/{postId}/comment/{commentId}")
    public ResponseEntity<Void> deleteCommentByAdmin(
            @AuthenticationPrincipal Long userId,
            @PathVariable String urlPath,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody AdminCommentDeleteRequestDTO request
    ){
        /* 삭제 로직*/
        communityAdminService.deleteComment(userId,request.reason(),commentId,urlPath,postId);
        return ResponseEntity.ok().build();
    }

}
