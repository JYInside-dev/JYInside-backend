package org.najeungyoon.communitybackend.service.OAuth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.configuration.KakaoOAuthProperties;
import org.najeungyoon.communitybackend.domain.auth.AuthProvider;
import org.najeungyoon.communitybackend.domain.auth.SocialAccount;
import org.najeungyoon.communitybackend.domain.app.User;
import org.najeungyoon.communitybackend.domain.app.UserRole;
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
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthService {
    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_PROFILE_URL = "https://kapi.kakao.com/v2/user/me";

    private final KakaoOAuthProperties kakaoOAuthProperties;
    private final SocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthTicketService oauthTicketService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.frontend-base-url:http://localhost}")
    private String frontendBaseUrl;

    @Transactional
    public String handleKakaoCallback(String code) {
        log.info("카카오 OAuth callback 처리 시작");

        KakaoTokenResponseDTO tokenResponse = requestKakaoToken(code);

        if (tokenResponse.accessToken() == null || tokenResponse.accessToken().isBlank()) {
            throw new IllegalArgumentException("카카오 access token을 받지 못했습니다.");
        }

        KakaoProfileResponseDTO profile = requestKakaoProfile(tokenResponse.accessToken());

        if (profile.id() == null) {
            throw new IllegalArgumentException("카카오 사용자 식별자를 가져오지 못했습니다.");
        }

        String providerUserId = String.valueOf(profile.id());
        String email = null;
        String nickname = null;
        String pictureUrl = null;

        if (profile.kakaoAccount() != null) {
            email = profile.kakaoAccount().email();

            if (profile.kakaoAccount().profile() != null) {
                nickname = profile.kakaoAccount().profile().nickname();
                pictureUrl = profile.kakaoAccount().profile().profileImageUrl();
            }
        }

        log.info("카카오 사용자 확인 완료. providerUserId={}", providerUserId);

        String finalEmail = email;
        String finalNickname = nickname;
        String finalPictureUrl = pictureUrl;

        return socialAccountRepository
                .findByProviderAndProviderUserId(AuthProvider.KAKAO, providerUserId)
                .map(this::createExistingUserRedirectUrl)
                .orElseGet(() -> createNewKakaoUserRedirectUrl(
                        providerUserId,
                        finalEmail,
                        finalNickname,
                        finalPictureUrl
                ));
    }

    private KakaoTokenResponseDTO requestKakaoToken (String code) {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", kakaoOAuthProperties.restApiKey());
        body.add("redirect_uri", kakaoOAuthProperties.redirectUri());
        body.add("grant_type", "authorization_code");

        if (StringUtils.hasText(kakaoOAuthProperties.clientSecret())) {
            body.add("client_secret", kakaoOAuthProperties.clientSecret());
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<KakaoTokenResponseDTO> response = restTemplate.postForEntity(
                    KAKAO_TOKEN_URL,
                    request,
                    KakaoTokenResponseDTO.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalArgumentException("카카오 토큰 요청에 실패했습니다.");
            }

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("카카오 토큰 요청 실패. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());

            throw new IllegalArgumentException("카카오 토큰 요청 실패: " + e.getResponseBodyAsString(), e);
        }
    }

    private KakaoProfileResponseDTO requestKakaoProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoProfileResponseDTO> response = restTemplate.exchange(
                    KAKAO_PROFILE_URL,
                    HttpMethod.GET,
                    request,
                    KakaoProfileResponseDTO.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalArgumentException("카카오 사용자 정보 조회에 실패했습니다.");
            }

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("카카오 사용자 조회 실패. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());

            throw new IllegalArgumentException("카카오 사용자 조회 실패:" + e.getResponseBodyAsString(), e);
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

    private String createNewKakaoUserRedirectUrl(
            String providerUserId,
            String email,
            String nickname,
            String pictureUrl
    ) {
        PendingSocialSignup pendingSocialSignup = new PendingSocialSignup(
                AuthProvider.KAKAO,
                providerUserId,
                email,
                nickname,
                pictureUrl
        );

        String ticket = oauthTicketService.createSocialSignupTicket(pendingSocialSignup);

        return UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path("/oauth/kakao/signup")
                .queryParam("ticket", ticket)
                .build()
                .toUriString();
    }

    @Transactional(readOnly = true)
    public KakaoSignupInfoResponseDTO getKakaoSignupInfo(String ticket) {
        PendingSocialSignup pending = oauthTicketService.getSocialSignupTicket(ticket, AuthProvider.KAKAO)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카카오 회원가입 티켓입니다."));

        return new KakaoSignupInfoResponseDTO(
                pending.name(),
                pending.pictureUrl()
        );
    }

    @Transactional
    public JwtTokenResponseDTO completeKakaoSignup(KakaoSignupCompleteRequestDTO dto) {
        PendingSocialSignup pending = oauthTicketService.consumeSocialSignupTicket(dto.ticket(), AuthProvider.KAKAO)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카카오 회원가입 티켓입니다."));

        validateKakaoSignup(dto, pending);

        User user = new User(
                dto.username(),
                dto.email(),
                null,
                dto.nickname(),
                UserRole.USER
        );

        userRepository.save(user);

        SocialAccount socialAccount = new SocialAccount(
                AuthProvider.KAKAO,
                pending.providerUserId(),
                dto.email(),
                user
        );

        socialAccountRepository.save(socialAccount);

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().getKey() // ROLE_* 형식의 문자열을 인자로 전송
        );

        return new JwtTokenResponseDTO(
                "Bearer",
                accessToken,
                user.getUsername(),
                user.getNickname()
        );
    }

    private void validateKakaoSignup(KakaoSignupCompleteRequestDTO dto, PendingSocialSignup pending) {
        if (pending.provider() != AuthProvider.KAKAO) {
            throw new IllegalArgumentException("카카오 회원가입 티켓이 아닙니다.");
        }

        if (dto.username() == null || dto.username().isBlank()) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }

        if (dto.nickname() == null || dto.nickname().isBlank()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }

        if (dto.email() == null || dto.email().isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        if (userRepository.existsByUsername(dto.username())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByNickname(dto.nickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (socialAccountRepository.existsByProviderAndProviderUserId(AuthProvider.KAKAO, pending.providerUserId())) {
            throw new IllegalArgumentException("이미 연결된 카카오 계정입니다.");
        }
    }
}
