package org.najeungyoon.communitybackend.controller;

import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.configuration.GoogleOAuthProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final GoogleOAuthProperties googleOAuthProperties;

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

    @GetMapping("/google/callback")
    public ResponseEntity<String> googleCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state
    ) {
        return ResponseEntity.ok("구글 callback 성공. code=" + code + ", state=" + state);
    }
}
