package org.najeungyoon.communitybackend.aop.aspects;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Slf4j
@Aspect
@Component
public class ViewTrackingAspect {

    @Around("@annotation(org.najeungyoon.communitybackend.aop.annotation.TrackView)")
    public Object trackView(ProceedingJoinPoint joinPoint) throws Throwable {


        /* http 요청정보 가져오기*/
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes())
                .getRequest();

        boolean mdcAdded = false;

        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (pathVariables != null) {
            // 2. 보따리 안에 "boardId"라는 열쇠(Key)가 있어?
            if (pathVariables.containsKey("urlPath")) {
                // 3. 있으면 그 값(Value = "1")을 꺼내서 MDC에 저장해!
                MDC.put("urlPath", pathVariables.get("urlPath"));
                mdcAdded=true;
            }
            if (pathVariables.containsKey("postId")) {
                // 값(Value = "15")을 꺼내서 MDC에 저장해!
                MDC.put("postId", pathVariables.get("postId"));
                mdcAdded=true;
            }

            // 💡 3. 값 추출이 잘 되었는지 확인하는 로그 한 줄 추가!
            if (mdcAdded) {
                log.info("🎯 [@TrackView AOP 작동] MDC 저장 완료 - urlPath: {}, postId: {}",
                        MDC.get("urlPath"), MDC.get("postId"));
            }
        }


        try {
            // 실제 컨트롤러 로직 실행 (이때 기존 LoggingAspect도 같이 돌면서 MDC 값을 로그에 찍습니다!)
            return joinPoint.proceed();
        } finally {
            // 스레드 풀 오염 방지를 위해 썼던 MDC 값은 반드시 청소해 줍니다.
            if (mdcAdded) {
                MDC.remove("urlPath");
                MDC.remove("postId");
            }
        }
    }
}
