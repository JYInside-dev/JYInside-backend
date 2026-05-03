package org.najeungyoon.communitybackend.repository;

import org.najeungyoon.communitybackend.domain.auth.OAuthTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthTicketRepository extends JpaRepository<OAuthTicket, Long> {
    // ticketHash가 일치하고 아직 사용되지 않은 ticket만 찾음
    Optional<OAuthTicket> findByTicketHashAndConsumedAtIsNull(String ticketHash);
}
