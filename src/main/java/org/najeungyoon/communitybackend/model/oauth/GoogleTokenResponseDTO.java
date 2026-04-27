package org.najeungyoon.communitybackend.model.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponseDTO(
        // 구글 토큰 API JSON 필드명이 snake_case로 오기 때문에 @JsonProperty로 연결
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        Long expiresIn,

        @JsonProperty("scope")
        String scope,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("id_token")
        String idToken
) {
}
