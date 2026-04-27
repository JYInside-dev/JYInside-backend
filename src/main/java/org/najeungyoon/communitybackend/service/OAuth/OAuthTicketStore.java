package org.najeungyoon.communitybackend.service.OAuth;

import org.najeungyoon.communitybackend.model.oauth.PendingGoogleSignup;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OAuthTicketStore {

    private final Map<String, Long> loginTickets = new ConcurrentHashMap<>();
    private final Map<String, PendingGoogleSignup> googleSignupTickets = new ConcurrentHashMap<>();

    public String createLoginTicket(Long userId) {
        String ticket = UUID.randomUUID().toString();
        loginTickets.put(ticket, userId);
        return ticket;
    }

    public Optional<Long> consumeLoginTicket(String ticket) {
        return Optional.ofNullable(loginTickets.remove(ticket));
    }

    public String createGoogleSignupTicket(PendingGoogleSignup pendingGoogleSignup) {
        String ticket = UUID.randomUUID().toString();
        googleSignupTickets.put(ticket, pendingGoogleSignup);
        return ticket;
    }

    public Optional<PendingGoogleSignup> getGoogleSignupTicket(String ticket) {
        return Optional.ofNullable(googleSignupTickets.get(ticket));
    }

    public Optional<PendingGoogleSignup> consumeGoogleSignupTicket(String ticket) {
        return Optional.ofNullable(googleSignupTickets.remove(ticket));
    }
}
