package org.najeungyoon.communitybackend.controller;

import io.jsonwebtoken.Jwt;
import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.configuration.GoogleOAuthProperties;
import org.najeungyoon.communitybackend.model.auth.JwtTokenResponseDTO;
import org.najeungyoon.communitybackend.model.oauth.GoogleSignupCompleteRequestDTO;
import org.najeungyoon.communitybackend.model.oauth.GoogleSignupInfoResponseDTO;
import org.najeungyoon.communitybackend.service.OAuth.GoogleOAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final GoogleOAuthProperties googleOAuthProperties;
    private final GoogleOAuthService googleOAuthService;

    // 사용자를 구글 로그인 페이지로 보내는 시작점
    @GetMapping("/google/login")
    public ResponseEntity<Void> googleLogin() {
        // OAuth 로그인 요청을 보낼때 state를 같이 보내고 callback으로 돌아왔을 때
        // 같은 state인지 확인하는게 보안상 좋음
        String state = UUID.randomUUID().toString();

        String googleAuthUrl = UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", googleOAuthProperties.clientId())
                .queryParam("redirect_uri", googleOAuthProperties.redirectUri())
                .queryParam("response_type", "code")
                // 구글에게 어떤 정보 접근을 요청할지 정함 / openid = 로그인 식별용, email = 이메일 정보, profile = 기본 프로필 정보
                .queryParam("scope", "openid email profile")
                .queryParam("state", state)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();

        return ResponseEntity
                .status(302)
                .location(URI.create(googleAuthUrl))
                .build();
    }

    // 구글 로그인 성공 후 돌아오는 곳
    @GetMapping("/google/callback")
    public ResponseEntity<Void> googleCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state
    ) {
        String redirectUrl = googleOAuthService.handleGoogleCallback(code);

        return ResponseEntity.status(302)
                .location(URI.create(redirectUrl))
                .build();
    }

    // 구글에서 받은 이메일/이름 미리보기
    @GetMapping("/google/signup-info")
    public ResponseEntity<GoogleSignupInfoResponseDTO> getGoogleSignupInfo(
            @RequestParam String ticket
    ) {
        GoogleSignupInfoResponseDTO response = googleOAuthService.getGoogleSignupInfo(ticket);

        return ResponseEntity.ok(response);
    }

    // JWT 발급
    @PostMapping("/google/signup/complete")
    public ResponseEntity<JwtTokenResponseDTO> completeGoogleSignup(
            @RequestBody GoogleSignupCompleteRequestDTO dto
    ) {
        JwtTokenResponseDTO response = googleOAuthService.completeGoogleSignup(dto);

        return ResponseEntity.ok(response);
    }

    // 프론트가 ticket을 우리 JWT로 교환하는 곳
    @GetMapping("/login-ticket")
    public ResponseEntity<JwtTokenResponseDTO> exchangeLoginTicket(
            @RequestParam String ticket
    ) {
        JwtTokenResponseDTO response = googleOAuthService.exchangeLoginTicket(ticket);
        return ResponseEntity.ok(response);
    }
}
