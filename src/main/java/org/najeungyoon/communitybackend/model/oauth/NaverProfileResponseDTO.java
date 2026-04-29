package org.najeungyoon.communitybackend.model.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverProfileResponseDTO(
        String resultcode,
        String message,
        Response response
) {
    public record Response(
            String id,
            String email,
            String nickname,

            @JsonProperty("profile_image")
            String profileImage
    ) {
    }
}
