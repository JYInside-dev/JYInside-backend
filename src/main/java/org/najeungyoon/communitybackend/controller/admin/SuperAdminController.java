package org.najeungyoon.communitybackend.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.model.board.BoardCreateRequestDTO;
import org.najeungyoon.communitybackend.service.admin.SuperAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin/super")
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

}
