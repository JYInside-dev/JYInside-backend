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
        String state = UUID.randomUUID().toString();

        String googleAuthUrl = UriComponentsBuilder
                .fromUriString("https://account.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", googleOAuthProperties.clientId())
                .queryParam("redirect_uri", googleOAuthProperties.redirectUri())
                .queryParam("response_type", "code")
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
