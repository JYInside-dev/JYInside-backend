package org.najeungyoon.communitybackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.model.auth.JwtTokenResponseDTO;
import org.najeungyoon.communitybackend.model.auth.LoginRequestDTO;
import org.najeungyoon.communitybackend.model.auth.SignupRequestDTO;
import org.najeungyoon.communitybackend.service.AuthService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> sighup(
            @RequestBody SignupRequestDTO dto
    ) {
        log.info("회원가입 API 요청 들어옴");
        authService.signup(dto);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponseDTO> login(
            @RequestBody LoginRequestDTO dto
    ) {
        log.info("로그인 API 요청 들어옴");
        JwtTokenResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(response);
    }
}
