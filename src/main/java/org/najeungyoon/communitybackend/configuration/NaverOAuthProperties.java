package org.najeungyoon.communitybackend.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.naver")
public record NaverOAuthProperties (
        String clientId,
        String clientSecret,
        String redirectUri
) {
}
