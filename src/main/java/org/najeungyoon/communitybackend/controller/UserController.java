package org.najeungyoon.communitybackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.model.user.NicknameUpdateRequestDTO;
import org.najeungyoon.communitybackend.model.user.PasswordChangeRequestDTO;
import org.najeungyoon.communitybackend.model.user.UserMeResponseDTO;
import org.najeungyoon.communitybackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserMeResponseDTO> getMe(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(userService.getMe(userId));
    }

    @PatchMapping("/me/nickname")
    public ResponseEntity<String> changeNickname(
            Authentication authentication,
            @Valid @RequestBody NicknameUpdateRequestDTO dto
    ) {
        Long userId = getUserId(authentication);
        userService.changeNickname(userId, dto.nickname());
        return ResponseEntity.ok("닉네임이 변경되었습니다.");
    }

    @PatchMapping("/me/password")
    public ResponseEntity<String> changePassword(
            Authentication authentication,
            @Valid @RequestBody PasswordChangeRequestDTO dto
    ) {
        Long userId = getUserId(authentication);
        userService.changePassword(userId, dto.currentPassword(), dto.newPassword());
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        return Long.valueOf(authentication.getName());
    }
}
