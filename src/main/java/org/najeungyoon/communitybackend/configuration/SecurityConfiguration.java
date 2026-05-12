package org.najeungyoon.communitybackend.configuration;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.jwt.JwtAuthenticationFilter;
import org.najeungyoon.communitybackend.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtTokenProvider jwtTokenProvider;

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

                                /* 추천에 로그인 인증 부여 */
                                .requestMatchers(HttpMethod.POST, "/api/community/board/*/post/*/likes/**").authenticated()

                                // 로그인 안해도 접근 가능한 주소 설정
                                .requestMatchers("/api/auth/**", "/api/oauth/**", "/api/community/**", "/actuator/**").permitAll()

                                /* API 접근 권한 설정 주소*/
                                .requestMatchers("/api/admin/super/**", "/api/ops/**").hasRole("SUPER_ADMIN")

                                /* 나머지 기능은 로그인 필요 */
                                .anyRequest().authenticated()

                        /* jwt인증 필터를 걸어서 controller에 요청이 가기전에 일단 한번 토큰 유효여부 확인. */
                ).addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
        ;


        /* 예외처리 필터*/
        http.exceptionHandling(exceptionHandling -> {

                    // 1. 권한이 없을 때 (403 Forbidden) - 예: USER가 SUPER_ADMIN 페이지 접근
                    exceptionHandling.accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"message\": \"해당 기능에 대한 접근 권한이 없습니다.\"}");
                    });

                    // 2. 인증되지 않았을 때 (401 Unauthorized) - 예: 토큰 없이 로그인 필수 페이지 접근
                    exceptionHandling.authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"message\": \"로그인이 필요하거나 토큰이 만료되었습니다.\"}");
                    });
                });



        return http.build();
    }
}
