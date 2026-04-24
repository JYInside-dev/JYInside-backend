package org.najeungyoon.communitybackend.model;

public record JwtTokenResponseDTO(
        // 토큰의 인증 방식 지정 (우린 JWT, OAuth 토큰을 사용해서 Bearer만 거의 사용)
        String tokenType,
        String accessToken,

        // 이 밑은 나중에 jwt로 사용자 확인 후 유저 정보 반환 식으로 변경 예정
        String username,
        String nickname
) {
}
