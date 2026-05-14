package org.najeungyoon.communitybackend.service.OAuth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.configuration.GoogleOAuthProperties;
import org.najeungyoon.communitybackend.domain.app.user.UserStatus;
import org.najeungyoon.communitybackend.domain.auth.AuthProvider;
import org.najeungyoon.communitybackend.domain.auth.SocialAccount;
import org.najeungyoon.communitybackend.domain.app.user.User;
import org.najeungyoon.communitybackend.domain.app.user.UserRole;
import org.najeungyoon.communitybackend.jwt.JwtTokenProvider;
import org.najeungyoon.communitybackend.model.auth.JwtTokenResponseDTO;
import org.najeungyoon.communitybackend.model.oauth.GoogleSignupCompleteRequestDTO;
import org.najeungyoon.communitybackend.model.oauth.GoogleSignupInfoResponseDTO;
import org.najeungyoon.communitybackend.model.oauth.GoogleTokenResponseDTO;
import org.najeungyoon.communitybackend.model.oauth.PendingSocialSignup;
import org.najeungyoon.communitybackend.repository.SocialAccountRepository;
import org.najeungyoon.communitybackend.repository.user.UserRepository;
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

/* code로 구글 토큰 요청
   id_token 검증
   구글 사용자 정보 추출
   기존 소셜 계정 조회
   기존 유저면 로그인 티켓 생성
   신규 유저면 회원가입 티켓 생성
   리다이렉트 주소 생성
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthService {

    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";

    private final GoogleOAuthProperties googleOAuthProperties;
    private final SocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthTicketService oauthTicketService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.frontend-base-url:http://localhost}")
    private String frontendBaseUrl;

    @Transactional
    public String handleGoogleCallback(String code) {
        log.info("구글 OAuth callback 처리 시작");

        // 구글이 callback으로 준 code를 구글 token endpoint로 보내서 id_token을 받아옴
        GoogleTokenResponseDTO tokenResponse = requestGoogleToken(code);

        if (tokenResponse.idToken() == null || tokenResponse.idToken().isBlank()) {
            throw new IllegalArgumentException("구글 ID 토큰을 받지 못했습니다.");
        }

        // id_token은 JWT같은 문자열. GoogleIdTokenVerifier를 써서 검증 후 payload를 꺼냄
        GoogleIdToken.Payload payload = verifyIdToken(tokenResponse.idToken());

        // subject는 구글 사용자의 고유 식별자. 이메일이 변경될 수 있으므로 구글 계정을 구분할 때 이 값으로 구분함
        String providerUserId = payload.getSubject();
        String email = payload.getEmail();
        Boolean emailVerified = payload.getEmailVerified();
        String name = (String)payload.get("name");
        // 구글 프로필 이미지 주소. 혹시 나중에 사용할 수도 있어서 넣어둠.
        String pictureUrl = (String)payload.get("picture");

        log.info("구글 사용자 확인 완료. providerUserId={}, email={}, emailVerified={}", providerUserId, email, emailVerified);

        return socialAccountRepository
                // DB에서 조회
                .findByProviderAndProviderUserId(AuthProvider.GOOGLE, providerUserId)
                // 있다면 이미 가입한 사람
                .map(this::createExistingUserRedirectUrl)
                // 없다면 처음 구글 로그인한 사람 -> 추가 정보 입력 페이지로 보냄
                .orElseGet(() -> createNewGoogleUserRedirectUrl(
                        providerUserId,
                        email,
                        name,
                        pictureUrl
                ));
    }

    // 구글 token endpoint에 POST 요청을 보내는 코드
    private GoogleTokenResponseDTO requestGoogleToken(String code) {
        HttpHeaders headers = new HttpHeaders();

        // 구글 token endpoint는 일반 JSON이 아니라 form형태로 보내는 방식
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", googleOAuthProperties.clientId());
        body.add("client_secret", googleOAuthProperties.clientSecret());
        body.add("redirect_uri", googleOAuthProperties.redirectUri());
        // OAuth에서 authorization code 방식으로 토큰 교환하겠다는 의미
        // 서버 간 통신(S2S)을 통해 액세스 토큰을 안전하게 발급받는 가장 표준적이고 보안성이 높은 인증 방식
        body.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        // 외부 HTTP API를 호출함. 구글 token URL로 POST 요청을 보내고, 응답 JSON을 GoogleTokenResponseDTO로 받아옴.
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

    // 구글 id_token 검증 코드
    private GoogleIdToken.Payload verifyIdToken(String idTokenString) {
        try {
            // id_token 안의 aud 라는 이 토큰이 어떤 앱을 대상으로 발급되었는지 알려주는 값을 확인함.
            // 우리 서비스 용 토큰인지 확인함.
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            ).setAudience(Collections.singletonList(googleOAuthProperties.clientId()))
                    .build();

            // 검증 성공하면 GoogleIdToken 객체를 돌려주고, 실패하면 null 또는 예외
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new IllegalArgumentException("구글 ID 토큰 검증에 실패했습니다.");
            }

            return idToken.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalArgumentException("구글 ID 토큰 검증 중 오류가 발생했습니다.", e);
        }
    }

    // 이미 가입된 구글 계정일 때 실행
    private String createExistingUserRedirectUrl(SocialAccount socialAccount) {
        // 연결된 User 꺼냄
        User user = socialAccount.getUser();

        // userId로 login ticket 생성
        String ticket = oauthTicketService.createLoginTicket(user.getId());

        // 프론트 /oauth/success?ticket=... 로 이동
        // JWT를 URL에 노출하지 않기 위해 ticket 사용
        return UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path("/oauth/success")
                .queryParam("ticket", ticket)
                .build()
                .toUriString();
    }

    // 처음 구글 로그인한 사람일 때 실행
    private String createNewGoogleUserRedirectUrl(
            String providerUserId,
            String email,
            String name,
            String pictureUrl
    ) {
        // 구글 정보를 PendingSocialSignup으로 묶음
        PendingSocialSignup pendingSocialSignup = new PendingSocialSignup(
                AuthProvider.GOOGLE,
                providerUserId,
                email,
                name,
                pictureUrl
        );

        // 묶은 정보로 signup ticket 생성
        String ticket = oauthTicketService.createSocialSignupTicket(pendingSocialSignup);

        // 프론트 /oauth/gogle/signup?ticket=... 로 이동
        // 이후 프론트에서 추가 정보를 입력받고 가입 완료 API를 호출함
        return UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path("/oauth/google/signup")
                .queryParam("ticket", ticket)
                .build()
                .toUriString();
    }

    // 기존 가입자가 구글 로그인 성공 후 /oauth/success?ticket=...로 왔을 때 쓰는 API
    @Transactional
    public JwtTokenResponseDTO exchangeLoginTicket(String ticket) {
        // ticket으로 userId를 찾고 바로 삭제
        Long userId = oauthTicketService.consumeLoginTicket(ticket)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 로그인 티켓입니다."));

        // userId로 User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // JWT 발급
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

    // 가입 정보 조회
    @Transactional(readOnly = true)
    public GoogleSignupInfoResponseDTO getGoogleSignupInfo(String ticket) {
        PendingSocialSignup pending = oauthTicketService.getSocialSignupTicket(ticket, AuthProvider.GOOGLE)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 구글 회원가입 티켓입니다."));

        return new GoogleSignupInfoResponseDTO(
                pending.email(),
                pending.name(),
                pending.pictureUrl()
        );
    }

    // 가입 완료
    @Transactional
    public JwtTokenResponseDTO completeGoogleSignup(GoogleSignupCompleteRequestDTO dto) {
        PendingSocialSignup pending = oauthTicketService.consumeSocialSignupTicket(dto.ticket(), AuthProvider.GOOGLE)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 구글 회원가입 티켓입니다."));

        validateGoogleSignup(dto, pending);

        User user = new User(
                dto.username(),
                pending.email(),
                null,
                dto.nickname(),
                UserRole.USER,
                UserStatus.ACTIVE
        );

        userRepository.save(user);

        SocialAccount socialAccount = new SocialAccount(
                AuthProvider.GOOGLE,
                pending.providerUserId(),
                pending.email(),
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

    private void validateGoogleSignup(GoogleSignupCompleteRequestDTO dto, PendingSocialSignup pending) {
        if (pending.provider() != AuthProvider.GOOGLE) {
            throw new IllegalArgumentException("구글 회원가입 티켓이 아닙니다.");
        }

        if (dto.username() == null || dto.username().isBlank()) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }

        if (dto.nickname() == null || dto.nickname().isBlank()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }

        if (pending.email() == null || pending.email().isBlank()) {
            throw new IllegalArgumentException("구글 계정에서 이메일을 가져오지 못했습니다.");
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

        if (socialAccountRepository.existsByProviderAndProviderUserId(AuthProvider.GOOGLE, pending.providerUserId())) {
            throw new IllegalArgumentException("이미 연결된 구글 계정입니다.");
        }
    }
}
