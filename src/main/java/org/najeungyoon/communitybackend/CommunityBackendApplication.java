package org.najeungyoon.communitybackend;

import org.najeungyoon.communitybackend.configuration.GoogleOAuthProperties;
import org.najeungyoon.communitybackend.configuration.KakaoOAuthProperties;
import org.najeungyoon.communitybackend.configuration.NaverOAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

// 설정 클래스를 실제 빈으로 등록해서 사용할 수 있도록 설정
@EnableConfigurationProperties({
        GoogleOAuthProperties.class,
        NaverOAuthProperties.class,
        KakaoOAuthProperties.class
})

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync // Async 비동기 작업 활성화
@EnableScheduling
public class CommunityBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityBackendApplication.class, args);
    }

}
