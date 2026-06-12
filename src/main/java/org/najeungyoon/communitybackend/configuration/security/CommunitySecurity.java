package org.najeungyoon.communitybackend.configuration.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.repository.BoardAdminRepository;
import org.najeungyoon.communitybackend.repository.user.UserCommunitySuspensionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component("communitySecurity")
@RequiredArgsConstructor
public class CommunitySecurity {

    private final BoardAdminRepository boardAdminRepository;
    private final UserCommunitySuspensionRepository userCommunitySuspensionRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");


    private static final long SUSPENSION_CACHE_TTL_MS = 30_000L;

    private final Map<String, SuspensionCacheValue> suspensionCache = new ConcurrentHashMap<>();

    private record SuspensionCacheValue(
            boolean suspended,
            String suspendUntil,
            long expiresAtMillis
    ) {
        boolean isExpired(long now) {
            return expiresAtMillis <= now;
        }
    }


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
                    String formattedDate = suspension.getSuspendUntil().format(formatter);
                    throw new AccessDeniedException("해당 커뮤니티에서 활동이 정지된 사용자입니다. 정지 해제 일시: " + formattedDate);
                }
            });

            String cacheKey = userId + ":" + urlPath;
            long now = System.currentTimeMillis();

            /* 캐시가 있다면 여기서 검사 */
            SuspensionCacheValue cached = suspensionCache.get(cacheKey);
            if (cached != null) {
                if (!cached.isExpired(now)) {
                    if (cached.suspended()) {
                        log.warn("정지된 유저({})가 커뮤니티({}) 접근 시도 - 캐시 사용", userId, urlPath);
                        throw new AccessDeniedException(
                                "해당 커뮤니티에서 활동이 정지된 사용자입니다. 정지 해제 일시: "
                                        + cached.suspendUntil()
                        );
                    }

                    return true;
                }

                suspensionCache.remove(cacheKey, cached);
            }

            /* DB 검사 */
            var suspensionOptional =
                    userCommunitySuspensionRepository.findByUser_IdAndBoard_UrlPath(userId, urlPath);

            /* 정지 유저라면 */
            if (suspensionOptional.isPresent() && suspensionOptional.get().isSuspend()) {
                var suspension = suspensionOptional.get();

                String suspendUntil = String.valueOf(suspension.getSuspendUntil());

                suspensionCache.put(
                        cacheKey,
                        new SuspensionCacheValue(
                                true,
                                suspendUntil,
                                now + SUSPENSION_CACHE_TTL_MS
                        )
                );

                log.warn("정지된 유저({})가 커뮤니티({}) 접근 시도", userId, urlPath);

                throw new AccessDeniedException(
                        "해당 커뮤니티에서 활동이 정지된 사용자입니다. 정지 해제 일시: "
                                + suspendUntil
                );
            }

            /* 정지 유저가 아니라면 */
            suspensionCache.put(
                    cacheKey,
                    new SuspensionCacheValue(
                            false,
                            null,
                            now + SUSPENSION_CACHE_TTL_MS
                    )
            );

//            userCommunitySuspensionRepository.findByUser_IdAndBoard_UrlPath(userId,urlPath).ifPresent((suspension)->{
//                if(suspension.isSuspend()){
//                    log.warn("정지된 유저({})가 커뮤니티({}) 접근 시도", userId,urlPath);
//                    throw new AccessDeniedException("해당 커뮤니티에서 활동이 정지된 사용자입니다. 정지 해제 일시: " + suspension.getSuspendUntil());
//                }
//            });
            return true;
        } catch (NumberFormatException e) {
            log.error("유저 ID 파싱 실패: {}", authentication.getName());
            return false;
        }

    }



}
