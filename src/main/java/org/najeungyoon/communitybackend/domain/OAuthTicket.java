package org.najeungyoon.communitybackend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

// 임시 티켓 저장 테이블
@Entity
@Getter
@NoArgsConstructor
@Table(name = "oauth_tickets")
public class OAuthTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 실제 ticket 원문을 저장하지 않고, 해시값을 저장
    @Column(nullable = false, unique = true, length = 100)
    private String ticketHash;

    // 티켓의 목적 저장
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OAuthTicketType type;

    // 티켓의 목적 세부 구분
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AuthProvider provider;

    // LOGIN 티켓에서 사용
    private Long userId;

    // GOOGLE_SIGHUP 티켓에서 사용
    @Column(length = 100)
    private String providerUserId;

    // 임시 사용자 정보
    @Column(length = 100)
    private String email;

    @Column(length = 100)
    private String name;

    @Column(length = 500)
    private String pictureUrl;

    // 티켓 만료 시간
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // 티켓 사용 시간
    private LocalDateTime consumedAt;

    // 티켓 생성 시간
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public OAuthTicket(
            String ticketHash,
            OAuthTicketType type,
            AuthProvider provider,
            Long userId,
            String providerUserId,
            String email,
            String name,
            String pictureUrl,
            LocalDateTime expiresAt
    ) {
        this.ticketHash = ticketHash;
        this.type = type;
        this.provider = provider;
        this.userId = userId;
        this.providerUserId = providerUserId;
        this.email = email;
        this.name = name;
        this.pictureUrl = pictureUrl;
        this.expiresAt = expiresAt;
    }

    // 티켓 사용 처리 메서드
    public void consume() {
        this.consumedAt = LocalDateTime.now();
    }

    // 티켓 만료 확인 메서드
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    // 티켓 사용 확인 메서드
    public boolean isConsumed() {
        return this.consumedAt != null;
    }
}
