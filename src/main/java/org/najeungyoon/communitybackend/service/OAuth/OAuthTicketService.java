package org.najeungyoon.communitybackend.service.OAuth;

import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.domain.AuthProvider;
import org.najeungyoon.communitybackend.domain.OAuthTicket;
import org.najeungyoon.communitybackend.domain.OAuthTicketType;
import org.najeungyoon.communitybackend.model.oauth.PendingSocialSignup;
import org.najeungyoon.communitybackend.repository.OAuthTicketRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;


/*
   백엔드가 프론트로 사용자 정보를 이동시킬 때 JWT를 URL에 직접 붙이면 보안상 위험함
   그래서 JWT 대신 짧은 임시 티켓을 URL에 붙여서 보내는 구조로 만듦
   프론트가 이 ticket을 가지고 다시 백엔드에 요청하면
   백엔드가 그걸 확인해서 JWT를 JSON으로 내려줌

   ticket을 DB에 저장해서 blue/green 어느 서버를 사용해도 처리 가능하도록 함
*/
@Component
@RequiredArgsConstructor
public class OAuthTicketService {

    // DB에 ticket을 저장하고 조회하기 위함
    private final OAuthTicketRepository oauthTicketRepository;
    // ticket 원문을 예측하기 어렵게 하기 위한 보안 랜덤 생성기
    private final SecureRandom secureRandom = new SecureRandom();

    // 티켓 유효 시간 - 10분
    private static final int TICKET_EXPIRE_MINUTES = 10;

    // 기존 가입자가 소셜 로그인 성공했을 때 호출
    @Transactional
    public String createLoginTicket(Long userId) {
        // 랜덤 rawTicket을 생성
        String rawTicket = generateTicket();
        // rawTicket을 hashTicket으로 해시
        String ticketHash = hashTicket(rawTicket);

        // DB에는 ticketHash와 userId 저장
        OAuthTicket oauthTicket = new OAuthTicket(
                ticketHash,
                OAuthTicketType.LOGIN,
                null,
                userId,
                null,
                null,
                null,
                null,
                LocalDateTime.now().plusMinutes(TICKET_EXPIRE_MINUTES)
        );

        oauthTicketRepository.save(oauthTicket);

        // 프론트에는 rawTicket 반환
        return rawTicket;
    }

    // 프론트가 /api/oauth/login-ticket?ticket=... 을 호출했을 때 쓰는 메서드
    @Transactional
    public Optional<Long> consumeLoginTicket(String ticket) {
        // 프론트가 보낸 rawTicket을 해시
        String ticketHash = hashTicket(ticket);

        // DB에서 같은 ticketHash 찾기
        Optional<OAuthTicket> optionalTicket = oauthTicketRepository.findByTicketHashAndConsumedAtIsNull(ticketHash);

        // 사용 안됐는지 확인
        if (optionalTicket.isEmpty()) {
            return Optional.empty();
        }

        OAuthTicket oauthTicket = optionalTicket.get();

        // ticket이 만료되었는지, LOGIN 타입인지 확인
        if (oauthTicket.isExpired() || oauthTicket.getType() != OAuthTicketType.LOGIN) {
            return Optional.empty();
        }

        // ticket을 consume 처리
        oauthTicket.consume();

        // userId 반환
        return Optional.ofNullable(oauthTicket.getUserId());
    }

    // 신규 소셜 가입 유저일 때 호출됨
    @Transactional
    public String createSocialSignupTicket(PendingSocialSignup pendingSocialSignup) {
        String rawTicket = generateTicket();
        String ticketHash = hashTicket(rawTicket);

        // 소셜 로그인 페이지에서 받은 정보를 저장해서 프론트에 넘김
        OAuthTicket oauthTicket = new OAuthTicket(
                ticketHash,
                OAuthTicketType.SOCIAL_SIGNUP,
                pendingSocialSignup.provider(),
                null,
                pendingSocialSignup.providerUserId(),
                pendingSocialSignup.email(),
                pendingSocialSignup.name(),
                pendingSocialSignup.pictureUrl(),
                LocalDateTime.now().plusMinutes(TICKET_EXPIRE_MINUTES)
        );

        oauthTicketRepository.save(oauthTicket);

        return rawTicket;
    }

    // 추가 정보 입력 페이지를 열 때 사용됨
    @Transactional(readOnly = true)
    public Optional<PendingSocialSignup> getSocialSignupTicket(
            String ticket,
            AuthProvider expectedProvider
    ) {
        String ticketHash = hashTicket(ticket);

        // 페이지를 열 때 정보만 보는것이기 때문에 ticket을 소비하지 않음
        return oauthTicketRepository.findByTicketHashAndConsumedAtIsNull(ticketHash)
                .filter(oauthTicket ->
                        oauthTicket.getType() == OAuthTicketType.SOCIAL_SIGNUP)
                .filter(oauthTicket -> oauthTicket.getProvider() == expectedProvider)
                .filter(oauthTicket ->
                        !oauthTicket.isExpired())
                .map(this::toPendingSocialSignup);
    }

    // 사용자가 추가 정보 입력 후 가입 완료를 눌렀을 때 사용되는 메서드
    @Transactional
    public Optional<PendingSocialSignup> consumeSocialSignupTicket(
            String ticket,
            AuthProvider expectedProvider
    ) {
        String ticketHash = hashTicket(ticket);

        // ticket 검증
        Optional<OAuthTicket> optionalTicket = oauthTicketRepository.findByTicketHashAndConsumedAtIsNull(ticketHash);

        if (optionalTicket.isEmpty()) {
            return Optional.empty();
        }

        OAuthTicket oauthTicket = optionalTicket.get();

        // 만료 확인, SOCIAL_SIGNUP 타입, 소셜 타입 확인
        if (oauthTicket.isExpired() ||
                oauthTicket.getType() != OAuthTicketType.SOCIAL_SIGNUP ||
                oauthTicket.getProvider() != expectedProvider
        ) {
            return Optional.empty();
        }

        // ticket consume 처리
        oauthTicket.consume();

        // PendingGoogleSignup으로 반환
        return Optional.of(toPendingSocialSignup(oauthTicket));
    }

    // DB에 저장된 OAuthTicket을 다시 기존에 쓰던 PendingSocialSignup 객체로 바꿔주는 메서드
    private PendingSocialSignup toPendingSocialSignup(OAuthTicket oauthTicket) {
        return new PendingSocialSignup(
                oauthTicket.getProvider(),
                oauthTicket.getProviderUserId(),
                oauthTicket.getEmail(),
                oauthTicket.getName(),
                oauthTicket.getPictureUrl()
        );
    }

    // 랜덤 ticket을 만드는 메서드
    private String generateTicket() {
        // randomUUID 보다 보안성 좋은 랜덤 티켓 생성
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    // ticket 원문을 SHA-256으로 해시하는 메서드
    // DB 저장용 ticketHash를 만듦
    private String hashTicket(String rawTicket) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(rawTicket.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hashedBytes);
        } catch (Exception e) {
            throw new IllegalArgumentException("OAuth ticket 해시 생성 실패", e);
        }
    }
}
