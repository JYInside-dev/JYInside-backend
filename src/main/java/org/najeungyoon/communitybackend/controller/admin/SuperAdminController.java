package org.najeungyoon.communitybackend.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.model.board.BoardCreateRequestDTO;
import org.najeungyoon.communitybackend.model.grant.GrantCommunityAdminRequestDTO;
import org.najeungyoon.communitybackend.model.user.BoardAdminUserResponseDTO;
import org.najeungyoon.communitybackend.service.admin.SuperAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin/super")
@PreAuthorize("hasRole('SUPER_ADMIN')") // 스프링이 알아서 필터에서 뽑아내 로컬스레드에 저장한 정보를 뽑아서씀
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    @PostMapping("/create/board")
    public ResponseEntity<String> createBoard(
            @Validated @RequestBody BoardCreateRequestDTO boardCreateRequestDTO,
            @AuthenticationPrincipal Long adminId
            ){
        log.info("SUPER_ADMIN({})의 새 커뮤니티 생성 요청", adminId);
        superAdminService.createBoard(boardCreateRequestDTO,adminId);

        return ResponseEntity.status(201).body("새 커뮤니티가 생성됐습니다.");
    }

    @PostMapping("/grant")
    public ResponseEntity<String> grantAdmin(
            @RequestBody GrantCommunityAdminRequestDTO dto
            ){
        superAdminService.grantCommunityAdmin(dto.userId(), dto.boardUrlPath());
        return ResponseEntity.ok("커뮤니티 관리자 권한이 성공적으로 부여됬습니다.");
    }


    /* 관리자 유저 리스트 불러오기 */
    @GetMapping("/board-admins")
    public ResponseEntity<List<BoardAdminUserResponseDTO>> getBoardAdmins(){
        List<BoardAdminUserResponseDTO> adminList = superAdminService.getAllAdmins();
        return ResponseEntity.ok(adminList);
    }


    /* 관리자 권한 박탈하기 */
    @DeleteMapping("/{urlPath}/board-admins/{userId}")
    public ResponseEntity<Void> revokeAdminRole(
            @PathVariable String urlPath,
            @PathVariable Long userId
    ){
        superAdminService.revokeAdminRole(userId,urlPath);
        return ResponseEntity.noContent().build();
    }

}
