package org.najeungyoon.communitybackend.model.oauth;

import org.najeungyoon.communitybackend.domain.AuthProvider;

// 구글 계정은 확인됐지만 아직 우리 서비스의 추가 정보 입력이 안 끝난 상태를 잠깐 들고 있을 객체
public record PendingSocialSignup(
        AuthProvider provider,
        String providerUserId,
        String email,
        String name,
        String pictureUrl
) {
}
