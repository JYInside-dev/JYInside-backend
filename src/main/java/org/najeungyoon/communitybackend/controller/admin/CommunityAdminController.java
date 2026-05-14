package org.najeungyoon.communitybackend.controller.admin;


import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.model.admin.AdminCommentDeleteRequestDTO;
import org.najeungyoon.communitybackend.model.admin.AdminWarningRequestDTO;
import org.najeungyoon.communitybackend.service.admin.CommunityAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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


}
