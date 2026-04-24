package org.najeungyoon.communitybackend.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component

    /*  토근 인증 필터
     *   앞으로 로그인이 필요한 기능이 있을 때 필터로 토큰유효여부 확인하고 하기. */
    public class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final JwtTokenProvider jwtTokenProvider;


        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {


    }
}
