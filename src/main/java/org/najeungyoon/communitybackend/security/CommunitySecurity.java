package org.najeungyoon.communitybackend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.repository.BoardAdminRepository;
import org.najeungyoon.communitybackend.repository.user.UserCommunitySuspensionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component("communitySecurity")
@RequiredArgsConstructor
public class CommunitySecurity {

    private final BoardAdminRepository boardAdminRepository;
    private final UserCommunitySuspensionRepository userCommunitySuspensionRepository;



    /* manager인지 확인 */
    @Transactional(readOnly = true)
    public boolean isManager(String urlPath){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            return false;
        }

        Long userId = Long.parseLong(authentication.getName());

        return boardAdminRepository.existsByUser_IdAndBoard_UrlPath(userId,urlPath);
    }



    /* 정지인지 확인 */

    @Transactional(readOnly = true)
    public boolean checkNotSuspended(String urlPath){

        if (urlPath == null || urlPath.isBlank()) {
            log.error("보안 검증 실패: urlPath 파라미터가 누락되었습니다.");
            return false;
        }

        /* SecurityContextHolder에서 userId 추출 */
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            return true;
        }

        try {
            Long userId = Long.parseLong(authentication.getName());
            userCommunitySuspensionRepository.findByUser_IdAndBoard_UrlPath(userId,urlPath).ifPresent((suspension)->{
                if(suspension.isSuspend()){
                    log.warn("정지된 유저({})가 커뮤니티({}) 접근 시도", userId,urlPath);
                    throw new AccessDeniedException("해당 커뮤니티에서 활동이 정지된 사용자입니다. 정지 해제 일시: " + suspension.getSuspendUntil());
                }
            });
            return true;
        } catch (NumberFormatException e) {
            log.error("유저 ID 파싱 실패: {}", authentication.getName());
            return false;
        }

    }



}
