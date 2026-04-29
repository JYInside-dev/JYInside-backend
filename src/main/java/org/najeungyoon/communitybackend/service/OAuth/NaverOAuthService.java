package org.najeungyoon.communitybackend.service.OAuth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.configuration.NaverOAuthProperties;
import org.najeungyoon.communitybackend.domain.AuthProvider;
import org.najeungyoon.communitybackend.domain.SocialAccount;
import org.najeungyoon.communitybackend.domain.User;
import org.najeungyoon.communitybackend.domain.UserRole;
import org.najeungyoon.communitybackend.jwt.JwtTokenProvider;
import org.najeungyoon.communitybackend.model.auth.JwtTokenResponseDTO;
import org.najeungyoon.communitybackend.model.oauth.*;
import org.najeungyoon.communitybackend.repository.SocialAccountRepository;
import org.najeungyoon.communitybackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverOAuthService {

    private static final String NAVER_TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    private static final String NAVER_PROFILE_URL = "https://openapi.naver.com/v1/nid/me";

    private final NaverOAuthProperties naverOAuthProperties;
    private final SocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthTicketService oauthTicketService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.frontend-base-url:http://localhost}")
    private String frontendBaseUrl;

    @Transactional
    public String handleNaverCallback(String code, String state) {
        log.info("네이버 OAuth callback 처리 시작. state={}", state);

        NaverTokenResponseDTO tokenResponse = requestNaverToken(code, state);

        if (tokenResponse.accessToken() == null || tokenResponse.accessToken().isBlank()) {
            throw new IllegalArgumentException("네이버 access token을 받지 못했습니다.");
        }

        NaverProfileResponseDTO profileResponse = requestNaverProfile(tokenResponse.accessToken());

        if (profileResponse.response() == null || profileResponse.response().id() == null) {
            throw new IllegalArgumentException("네이버 사용자 식별자를 가져오지 못했습니다.");
        }

        String providerUserid = profileResponse.response().id();
        String email = profileResponse.response().email();
        String nickname = profileResponse.response().nickname();
        String pictureUrl = profileResponse.response().profileImage();

        log.info("네이버 사용자 확인 완료. providerUserId={}", providerUserid);

        return socialAccountRepository
                .findByProviderAndProviderUserId(AuthProvider.NAVER, providerUserid)
                .map(this::createExistingUserRedirectUrl)
                .orElseGet(() -> createNewNaverUserRedirectUrl(
                        providerUserid,
                        email,
                        nickname,
                        pictureUrl
                ));
    }

    private NaverTokenResponseDTO requestNaverToken(String code, String state) {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", naverOAuthProperties.clientId());
        body.add("client_secret", naverOAuthProperties.clientSecret());
        body.add("state", state);
        body.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<NaverTokenResponseDTO> response = restTemplate.postForEntity(
                    URI.create(NAVER_TOKEN_URL),
                    request,
                    NaverTokenResponseDTO.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalArgumentException("네이버 토큰 요청에 실패했습니다.");
            }

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("네이버 토큰 요청 실패. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());

            throw new IllegalArgumentException("네이버 토큰 요청 실패: " + e.getResponseBodyAsString(), e);
        }
    }

    private NaverProfileResponseDTO requestNaverProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<NaverProfileResponseDTO> response = restTemplate.exchange(
                    NAVER_PROFILE_URL,
                    HttpMethod.GET,
                    request,
                    NaverProfileResponseDTO.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalArgumentException("네이버 프로필 조회에 실패했습니다.");
            }

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("네이버 프로필 조회 실패. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());

            throw new IllegalArgumentException("네이버 프로필 조회 실패:" + e.getResponseBodyAsString(), e);
        }
    }

    private String createExistingUserRedirectUrl(SocialAccount socialAccount) {
        User user = socialAccount.getUser();

        String ticket = oauthTicketService.createLoginTicket(user.getId());

        return UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path("/oauth/success")
                .queryParam("ticket", ticket)
                .build()
                .toUriString();
    }

    private String createNewNaverUserRedirectUrl(
            String providerUserId,
            String email,
            String nickname,
            String pictureUrl
    ) {
        PendingSocialSignup pendingSocialSignup = new PendingSocialSignup(
                AuthProvider.NAVER,
                providerUserId,
                email,
                nickname,
                pictureUrl
        );

        String ticket = oauthTicketService.createSocialSignupTicket(pendingSocialSignup);

        return UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path("/oauth/naver/signup")
                .queryParam("ticket", ticket)
                .build()
                .toUriString();
    }

    @Transactional(readOnly = true)
    public NaverSignupInfoResponseDTO getNaverSignupInfo(String ticket) {
        PendingSocialSignup pending = oauthTicketService.getSocialSignupTicket(ticket, AuthProvider.NAVER)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 네이버 회원가입 티켓입니다."));

        return new NaverSignupInfoResponseDTO(
                pending.name(),
                pending.pictureUrl()
        );
    }

    @Transactional
    public JwtTokenResponseDTO completeNaverSignup(NaverSignupCompleteRequestDTO dto) {
        PendingSocialSignup pending = oauthTicketService.consumeSocialSignupTicket(dto.ticket(), AuthProvider.NAVER)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 네이버 회원가입 티켓입니다."));

        validateNaverSignup(dto, pending);

        User user = new User(
                dto.username(),
                dto.email(),
                null,
                dto.nickname(),
                UserRole.USER
        );

        userRepository.save(user);

        SocialAccount socialAccount = new SocialAccount(
                AuthProvider.NAVER,
                pending.providerUserId(),
                pending.email(),
                user
        );

        socialAccountRepository.save(socialAccount);

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

    private void validateNaverSignup(NaverSignupCompleteRequestDTO dto, PendingSocialSignup pending) {
        if (pending.provider() != AuthProvider.NAVER) {
            throw new IllegalArgumentException("네이버 회원가입 티켓이 아닙니다.");
        }

        if (dto.username() == null || dto.username().isBlank()) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }

        if (dto.nickname() == null || dto.nickname().isBlank()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }

        if (pending.email() == null || pending.email().isBlank()) {
            throw new IllegalArgumentException("네이버 계정에서 이메일을 가져오지 못했습니다.");
        }

        if (userRepository.existsByUsername(dto.username())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByNickname(dto.nickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        if (userRepository.existsByEmail(pending.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (socialAccountRepository.existsByProviderAndProviderUserId(AuthProvider.NAVER, pending.providerUserId())) {
            throw new IllegalArgumentException("이미 연결된 네이버 계정입니다.");
        }
    }
}
