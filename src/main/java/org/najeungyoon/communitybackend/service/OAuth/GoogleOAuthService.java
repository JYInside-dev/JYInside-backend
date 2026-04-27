package org.najeungyoon.communitybackend.service.OAuth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.configuration.GoogleOAuthProperties;
import org.najeungyoon.communitybackend.domain.AuthProvider;
import org.najeungyoon.communitybackend.domain.SocialAccount;
import org.najeungyoon.communitybackend.domain.User;
import org.najeungyoon.communitybackend.jwt.JwtTokenProvider;
import org.najeungyoon.communitybackend.model.auth.JwtTokenResponseDTO;
import org.najeungyoon.communitybackend.model.oauth.GoogleTokenResponseDTO;
import org.najeungyoon.communitybackend.model.oauth.PendingGoogleSignup;
import org.najeungyoon.communitybackend.repository.SocialAccountRepository;
import org.najeungyoon.communitybackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthService {

    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";

    private final GoogleOAuthProperties googleOAuthProperties;
    private final SocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthTicketStore oauthTicketStore;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.frontend-base-url:http://localhost}")
    private String frontendBaseUrl;

    @Transactional(readOnly = true)
    public String handleGoogleCallback(String code) {
        log.info("구글 OAuth callback 처리 시작");

        GoogleTokenResponseDTO tokenResponse = requestGoogleToken(code);

        if (tokenResponse.idToken() == null || tokenResponse.idToken().isBlank()) {
            throw new IllegalArgumentException("구글 ID 토큰을 받지 못했습니다.");
        }

        GoogleIdToken.Payload payload = verifyIdToken(tokenResponse.idToken());

        String providerUserId = payload.getSubject();
        String email = payload.getEmail();
        Boolean emailVerified = payload.getEmailVerified();
        String name = (String)payload.get("name");
        String pictureUrl = (String)payload.get("picture");

        log.info("구글 사용자 확인 완료. providerUserId={}, email={}, emailVerified={}", providerUserId, email, emailVerified);

        return socialAccountRepository
                .findByProviderAndProviderUserId(AuthProvider.GOOGLE, providerUserId)
                .map(this::createExistingUserRedirectUrl)
                .orElseGet(() -> createNewGoogleUserRedirectUrl(providerUserId, email, name, pictureUrl));
    }

    private GoogleTokenResponseDTO requestGoogleToken(String code) {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", googleOAuthProperties.clientId());
        body.add("client_secret", googleOAuthProperties.clientSecret());
        body.add("redirect_uri", googleOAuthProperties.redirectUri());
        body.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<GoogleTokenResponseDTO> response = restTemplate.postForEntity(
                GOOGLE_TOKEN_URL,
                request,
                GoogleTokenResponseDTO.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalArgumentException("구글 토큰 요청에 실패했습니다.");
        }

        return response.getBody();
    }

    private GoogleIdToken.Payload verifyIdToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            ).setAudience(Collections.singletonList(googleOAuthProperties.clientId()))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new IllegalArgumentException("구글 ID 토큰 검증에 실패했습니다.");
            }

            return idToken.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalArgumentException("구글 ID 토큰 검증 중 오류가 발생했습니다.", e);
        }
    }

    private String createExistingUserRedirectUrl(SocialAccount socialAccount) {
        User user = socialAccount.getUser();

        String ticket = oauthTicketStore.createLoginTicket(user.getId());

        return UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path("/oauth/success")
                .queryParam("ticket", ticket)
                .build()
                .toUriString();
    }

    private String createNewGoogleUserRedirectUrl(
            String providerUserId,
            String email,
            String name,
            String pictureUrl
    ) {
        PendingGoogleSignup pendingGoogleSignup = new PendingGoogleSignup(
                providerUserId,
                email,
                name,
                pictureUrl
        );

        String ticket = oauthTicketStore.createGoogleSignupTicket(pendingGoogleSignup);

        return UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path("/oauth/google/signup")
                .queryParam("ticket", ticket)
                .build()
                .toUriString();
    }

    @Transactional(readOnly = true)
    public JwtTokenResponseDTO exchangeLoginTicket(String ticket) {
        Long userId = oauthTicketStore.consumeLoginTicket(ticket)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 로그인 티켓입니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                String.valueOf(user.getRole())
        );

        return new JwtTokenResponseDTO(
                "Bearer",
                accessToken,
                user.getUsername(),
                user.getNickname()
        );
    }
}
