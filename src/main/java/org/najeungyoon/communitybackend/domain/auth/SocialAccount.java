package org.najeungyoon.communitybackend.domain.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.najeungyoon.communitybackend.domain.app.User;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(
        name = "social_accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_social_provider_user",
                        columnNames = {
                                "provider",
                                "provider_user_id"
                        }
                )
        }
)
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 외부 로그인인지: GOOGLE, NAVER, KAKAO
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    // 외부 서비스가 주는 고유 사용자 ID
    @Column(name = "provider_user_id", nullable = false, length = 100)
    private String providerUserId;

    // 외부 서비스에서 받은 이메일. 없을 수도 있어서 nullable 허용
    @Column(length = 100)
    private String providerEmail;

    // 여러 개의 SocialAccount가 하나의 User에 연결 가능 / 불필요한 DB 조회를 줄이기 위해 지연 로딩
    @ManyToOne(fetch = FetchType.LAZY)
    // social_accounts.user_id -> users.id
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public SocialAccount(AuthProvider provider, String providerUserId, String providerEmail, User user) {
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.providerEmail = providerEmail;
        this.user = user;
    }
}
