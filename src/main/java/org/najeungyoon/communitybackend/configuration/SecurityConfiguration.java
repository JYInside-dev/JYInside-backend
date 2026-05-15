package org.najeungyoon.communitybackend.configuration;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.jwt.JwtAuthenticationFilter;
import org.najeungyoon.communitybackend.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // PreAuthorize어노테이션 사용을 위한 추가
public class SecurityConfiguration {

    private final JwtTokenProvider jwtTokenProvider;

    /* Spring MVC의 예외 처리기 */
    private final HandlerExceptionResolver exceptionResolver;




    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 기본 브라우저, form 로그인 없어도 접속 가능하도록 설정 끄기 (Spring Security를 추가하면 기본적으로 보안이 켜지기 때문)
        http.csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable) // JWT환경에서는 CSRF공격 방어 옵션 끄기


                // 1. [매우 중요] 세션 관리 정책을 STATELESS로 설정
                // (Spring Security가 세션을 만들지도, 쓰지도 않게 하여 JWT 방식으로만 동작하게 함)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) .authorizeHttpRequests(auth -> auth

                                // 로그인 안해도 접근 가능한 주소 설정
                                .requestMatchers("/api/auth/**", "/api/oauth/**", "/api/community/**", "/actuator/**").permitAll()
                                /* 나머지 기능은 로그인 필요 */
                                .anyRequest().authenticated()

                        /* jwt인증 필터를 걸어서 controller에 요청이 가기전에 토큰 확인, Authentication 객체 생성 */
                ).addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
        ;


        /* 예외처리 필터*/
        http.exceptionHandling(exceptionHandling -> {
                    // 1. 권한이 없을 때 (403 Forbidden)
                    exceptionHandling.accessDeniedHandler((request, response, accessDeniedException) -> {
                        exceptionResolver.resolveException(request, response, null, accessDeniedException);
                    });
                    // 2. 인증되지 않았을 때 (401 Unauthorized)
                    exceptionHandling.authenticationEntryPoint((request, response, authException) -> {
                        exceptionResolver.resolveException(request, response, null, authException);
                    });
                });

        return http.build();
    }


    /* 생성자 */
    public SecurityConfiguration(JwtTokenProvider jwtTokenProvider,
                                 @Qualifier("handlerExceptionResolver")
                                 HandlerExceptionResolver exceptionResolver) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.exceptionResolver = exceptionResolver;
    }
}
