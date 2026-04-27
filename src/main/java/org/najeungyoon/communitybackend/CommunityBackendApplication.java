package org.najeungyoon.communitybackend;

import org.najeungyoon.communitybackend.configuration.GoogleOAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

// 설정 클래스를 실제 빈으로 등록해서 사용할 수 있도록 설정
@EnableConfigurationProperties(GoogleOAuthProperties.class)
@SpringBootApplication
public class CommunityBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityBackendApplication.class, args);
    }

}
