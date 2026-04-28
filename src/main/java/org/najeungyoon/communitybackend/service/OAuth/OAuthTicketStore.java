package org.najeungyoon.communitybackend.service.OAuth;

import org.najeungyoon.communitybackend.model.oauth.PendingGoogleSignup;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


// 실제 서비스를 위해 이후에는 DB 또는 캐시 또는 세션 저장소 형태로 변경하는게 좋음
@Component
public class OAuthTicketStore {

    private final Map<String, Long> loginTickets = new ConcurrentHashMap<>();
    private final Map<String, PendingGoogleSignup> googleSignupTickets = new ConcurrentHashMap<>();

    public String createLoginTicket(Long userId) {
        String ticket = UUID.randomUUID().toString();
        loginTickets.put(ticket, userId);
        return ticket;
    }

    // 보안때문에 티켓이 바로 사라지게 함
    public Optional<Long> consumeLoginTicket(String ticket) {
        return Optional.ofNullable(loginTickets.remove(ticket));
    }

    public String createGoogleSignupTicket(PendingGoogleSignup pendingGoogleSignup) {
        String ticket = UUID.randomUUID().toString();
        googleSignupTickets.put(ticket, pendingGoogleSignup);
        return ticket;
    }

    // 구글 로그인은 했지만 게시판 회원가입은 안 된 사람을 임시 저장함
    public Optional<PendingGoogleSignup> getGoogleSignupTicket(String ticket) {
        return Optional.ofNullable(googleSignupTickets.get(ticket));
    }

    public Optional<PendingGoogleSignup> consumeGoogleSignupTicket(String ticket) {
        return Optional.ofNullable(googleSignupTickets.remove(ticket));
    }
}
