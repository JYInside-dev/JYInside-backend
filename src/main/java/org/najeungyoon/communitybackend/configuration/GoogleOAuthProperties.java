package org.najeungyoon.communitybackend.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

// 구글 OAuth 설정값들을 깔끔하게 한 객체로 묶어서 사용하기 위함
@ConfigurationProperties(prefix = "oauth.google")
public record GoogleOAuthProperties(
        String clientId,
        String clientSecret,
        String redirectUri
) {
}
