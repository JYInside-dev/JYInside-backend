package org.najeungyoon.communitybackend.controller.admin;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.model.admin.AdminCommentDeleteRequestDTO;
import org.najeungyoon.communitybackend.model.admin.AdminWarningRequestDTO;
import org.najeungyoon.communitybackend.model.category.CategoryCreateResponseDTO;
import org.najeungyoon.communitybackend.model.category.CategoryReorderDTO;
import org.najeungyoon.communitybackend.model.category.CategoryResponseDTO;
import org.najeungyoon.communitybackend.model.category.CategoryStatusChangeDTO;
import org.najeungyoon.communitybackend.model.post.PostResponseDTO;
import org.najeungyoon.communitybackend.model.user.SuspendedUserResponseDTO;
import org.najeungyoon.communitybackend.repository.user.UserCommunitySuspensionRepository;
import org.najeungyoon.communitybackend.service.PostService;
import org.najeungyoon.communitybackend.service.admin.CommunityAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    private final UserCommunitySuspensionRepository userCommunitySuspensionRepository;



    /* 어드민 권한으로 댓글 삭제 */
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


    /* 경고주기 경고3회 -> 일주일 정지 */
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


    /* 정지된 유저 가져오기 */
    @GetMapping("/suspended/users")
    public ResponseEntity<Page<SuspendedUserResponseDTO>> getSuspendedUsers(
            @AuthenticationPrincipal Long adminId,
            @PathVariable String urlPath,
            @PageableDefault(size = 20, sort = "lastSuspendedAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<SuspendedUserResponseDTO> response = communityAdminService.getSuspendedUsers(urlPath, pageable);
        return ResponseEntity.ok(response);
    }


    /* 정지 해제하기 */
    @PatchMapping("/user/{userId}/liftSuspension")
    public ResponseEntity<Void> liftSuspension(
            @AuthenticationPrincipal Long adminId,
            @PathVariable Long userId,
            @PathVariable String urlPath
    ){
        communityAdminService.liftSuspension(adminId,userId,urlPath);
        return ResponseEntity.ok().build();
    }





    /* 글 고정/해제 */
    @PatchMapping("/posts/{postId}/pin")
    public ResponseEntity<PostResponseDTO> togglePinned(
            @PathVariable String postId,
            @PathVariable String urlPath
    ) {
        Long id = Long.parseLong(postId);
        return ResponseEntity.ok(postService.togglePinned(id));
    }




    /* 카테고리 생성하기 */
    @PostMapping("/category")
    public ResponseEntity<Void> createCategory(
            @PathVariable String urlPath,
            @Valid @RequestBody CategoryCreateResponseDTO createResponseDTO
            ){
        communityAdminService.createCategory(urlPath,createResponseDTO);
        return ResponseEntity.ok().build();
    }

    /* 카테고리 상태 변경하기 */
    @PatchMapping("/category/{categoryId}/status")
    public ResponseEntity<Void> deactivateCategory(
            @PathVariable String urlPath,
            @PathVariable Long categoryId,
            @RequestBody @Valid CategoryStatusChangeDTO statusChangeDTO
            ){

        communityAdminService.changeCategoryStatus(urlPath,categoryId,statusChangeDTO);
        return ResponseEntity.ok().build();
    }



    /* 카테고리 순서 바꾸기 하기 */
    @PatchMapping("/category/reorder")
    public ResponseEntity<Void> reorderCategory(
            @PathVariable String urlPath,
            @RequestBody CategoryReorderDTO reorderDTO
            ){

        communityAdminService.reorderCategory(urlPath,reorderDTO.categoryIdList());
        return ResponseEntity.ok().build();
    }


    /* 모든 카테고리 목록 가져오기 */
    @GetMapping("/categories/all")
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories(
            @PathVariable String urlPath
    ){

        List<CategoryResponseDTO> categoryResponseDTOList= communityAdminService.getAllCategory(urlPath);
        return ResponseEntity.ok(categoryResponseDTOList);
    }

}
