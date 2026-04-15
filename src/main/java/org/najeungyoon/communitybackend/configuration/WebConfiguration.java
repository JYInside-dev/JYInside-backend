package org.najeungyoon.communitybackend.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/* API를 가져가서 쓸 수 있는 주소 추가 */


@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 서버의 모든 API 경로에 대해
                .allowedOrigins("http://localhost:5173","http://localhost:3000") // React 서버의 접근을 허용 + nginx도
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 권한들
                .allowedHeaders("*")
                .allowCredentials(true)
                // cors정보를 캐싱할 시간 추가
                .maxAge(3600) ;
    }
}
