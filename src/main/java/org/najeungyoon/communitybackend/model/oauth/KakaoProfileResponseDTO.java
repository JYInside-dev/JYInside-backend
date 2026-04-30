package org.najeungyoon.communitybackend.model.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record KakaoProfileResponseDTO (
        Long id,

        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount,

        Map<String, Object> properties
) {
    public record KakaoAccount (
            String email,
            Profile profile
    ) {
    }

    public record Profile (
            String nickname,

            @JsonProperty("profile_image_url")
            String profileImageUrl,

            @JsonProperty("thumbnail_image_url")
            String thumbnailImageUrl
    ) {
    }
}
