package org.najeungyoon.communitybackend.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
@Component
@Slf4j
    /*  토근 인증 필터
     *   앞으로 로그인이 필요한 기능이 있을 때 필터로 토큰유효여부 확인하고 하기. */
    public class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final JwtTokenProvider jwtTokenProvider;


        /* 최종적으로 로그인토큰 유효여부 검증하는 메서드 */
        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {
            String token = resolveToken(request);

            /* 토큰 인증 후에 */
            if(token != null && jwtTokenProvider.validateToken(token)){

                /* 토큰내부값을 확인해서 인증이 부여된 객체를 return */
                Authentication authentication = getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request,response);
        }


        /* Authorization: Bearer <JWT> 형식에서 <JWT> 문자열만 뽑아내는 메서드 */
        private String resolveToken(HttpServletRequest request){
            String bearerToken = request.getHeader("Authorization");
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }else {
                return null;
            }
        }


        /* token확인 후 Authentication(인증) 부여하는 메서드 */
        private Authentication getAuthentication(String token){
            Long userId = jwtTokenProvider.getUserId(token);
            String role = jwtTokenProvider.getRole(token);

            return new UsernamePasswordAuthenticationToken(
                    userId, // principal 주체
                    null, // 자격증명: 이미 토큰을 가지고 있는거로 증명해서 null
                    Collections.singletonList(new SimpleGrantedAuthority(role)) // 권한
            );
        }




}
