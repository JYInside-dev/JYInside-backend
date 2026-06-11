package org.najeungyoon.communitybackend.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.model.auth.JwtTokenResponseDTO;
import org.najeungyoon.communitybackend.model.auth.LoginRequestDTO;
import org.najeungyoon.communitybackend.model.auth.SignupRequestDTO;
import org.najeungyoon.communitybackend.model.user.FindUsernameRequestDTO;
import org.najeungyoon.communitybackend.model.user.FindUsernameResponseDTO;
import org.najeungyoon.communitybackend.model.user.ResetPasswordRequestDTO;
import org.najeungyoon.communitybackend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(
            @Validated @RequestBody SignupRequestDTO dto
    ) {
        log.info("회원가입 API 요청 들어옴");
        authService.signup(dto);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }


    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponseDTO> login(
            @Validated @RequestBody LoginRequestDTO dto
    ) {
        log.info("로그인 API 요청 들어옴");
        JwtTokenResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/find-username")
    public ResponseEntity<FindUsernameResponseDTO> findUsername(
            @Validated @RequestBody FindUsernameRequestDTO dto
    ) {
        FindUsernameResponseDTO response = authService.findUsername(dto);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Validated @RequestBody ResetPasswordRequestDTO dto
    ) {
        authService.resetPassword(dto);
        return ResponseEntity.ok("비밀번호가 재설정되었습니다.");
    }
}
