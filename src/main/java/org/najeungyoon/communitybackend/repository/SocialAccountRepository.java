package org.najeungyoon.communitybackend.repository;

import org.najeungyoon.communitybackend.domain.auth.AuthProvider;
import org.najeungyoon.communitybackend.domain.auth.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(
            AuthProvider provider,
            String providerUserId
    );

    boolean existsByProviderAndProviderUserId(
            AuthProvider provider,
            String providerUserId
    );
}
