package org.najeungyoon.communitybackend.aspects;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.najeungyoon.communitybackend.domain.app.Board;
import org.najeungyoon.communitybackend.domain.app.user.User;
import org.najeungyoon.communitybackend.repository.BoardRepository;
import org.najeungyoon.communitybackend.repository.user.UserCommunitySuspensionRepository;
import org.najeungyoon.communitybackend.repository.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CommunitySuspensionAspect {

    private final UserCommunitySuspensionRepository userCommunitySuspensionRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;


    @Before("@annotation(org.najeungyoon.communitybackend.annotations.CheckCommunitySuspension)")
    public void checkSuspension(JoinPoint jp){

        /* SecurityContextHolder에서 userId 추출 */
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            return; // 비로그인 유저는 예외 처리 없이 통과 (Security 설정에 따라 달라질 수 있음)
        }
        Long userId = Long.parseLong(authentication.getName());


        /* RequestContextHolder에서 urlPath 추출 */
        HttpServletRequest request = ((
                ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String urlPath = pathVariables.get("urlPath");

        if (urlPath == null) {
            throw new IllegalArgumentException("커뮤니티 경로(urlPath)를 찾을 수 없습니다.");
        }


        /* User Board  select 하기 */
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        Board board = boardRepository.findByUrlPath(urlPath)
                .orElseThrow(() -> new IllegalArgumentException("커뮤니티를 찾을 수 없습니다."));


        userCommunitySuspensionRepository.findByUserAndBoard(user,board).ifPresent((suspension)->{
            if(suspension.isSuspend()){
                log.warn("정지된 유저({})가 커뮤니티({}) 접근 시도", user.getNickname(), board.getName());

                throw new IllegalStateException("해당 커뮤니티에서 활동이 정지된 사용자입니다. 정지 해제 일시: " + suspension.getSuspendUntil());
            }
        });
    }

}
