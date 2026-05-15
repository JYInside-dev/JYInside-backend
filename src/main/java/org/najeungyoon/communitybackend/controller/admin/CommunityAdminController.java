package org.najeungyoon.communitybackend.controller.admin;


import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.model.admin.AdminCommentDeleteRequestDTO;
import org.najeungyoon.communitybackend.model.admin.AdminWarningRequestDTO;
import org.najeungyoon.communitybackend.model.post.PostResponseDTO;
import org.najeungyoon.communitybackend.service.PostService;
import org.najeungyoon.communitybackend.service.admin.CommunityAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/community/board/{urlPath}")
/* 슈퍼어드민이거나 관리자일 경우에만 허용 */
@PreAuthorize("hasRole('SUPER_ADMIN') or @communitySecurity.isManager(#urlPath)")
@RequiredArgsConstructor
public class CommunityAdminController {

    private final CommunityAdminService communityAdminService;
    private final PostService postService;


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


    @PostMapping("/user/{targetUserId}/warning")
    public ResponseEntity<Void> giveWarning(
            @AuthenticationPrincipal Long adminId,
            @PathVariable Long targetUserId,
            @PathVariable String urlPath,
            @RequestBody AdminWarningRequestDTO requestDTO
            ){
        communityAdminService.giveWarning(adminId,targetUserId,urlPath,requestDTO.reason());
        return ResponseEntity.ok().build();
    }



    /* 글 고정/해제 */
    @PatchMapping("/posts/{postId}/pin")
    public ResponseEntity<PostResponseDTO> togglePinned(
            @PathVariable String postId
    ) {
        Long id = Long.parseLong(postId);
        return ResponseEntity.ok(postService.togglePinned(id));
    }

}
