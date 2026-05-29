package org.najeungyoon.communitybackend.aspects;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Aspect
@Slf4j
@Component
public class LoggingAspect {


    /* 코드 짜면서 볼 AOP 용어 정리
    *
    * Aspect (애스펙트): 공통 기능 모듈 그 자체입니다. (예: "이 클래스는 로깅을 담당하는 Aspect야!")
    * JoinPoint (조인포인트): 기능이 끼어들 수 있는 '시점'입니다. 스프링 AOP에서는 **'메서드 실행 시점'**만을 의미합니다.
    * Pointcut (포인트컷): 수많은 메서드 중에서 **"정확히 어느 컨트롤러나 서비스의 메서드에 로그를 남길 것인가?"**를 필터링하는 조건식입니다.
    * Advice (어드바이스): 타겟 메서드를 기준으로 **"언제(실행 전, 후), 어떤 로그를 찍을 것인가?"**를 정의하는 실제 코드입니다.
    *                    (주로 양옆을 모두 제어하는 @Around를 가장 많이 씁니다.)
    *
    * */


    /* 모든 컨트롤러의 모든 메소드에 포인트컷
    *  포인트컷을 지정하기 위한 메서드  */
    @Pointcut("execution(* org.najeungyoon.communitybackend.controller..*.*(..))")
    public void controllerPointcut(){}


    /* 어드바이스: 포인트컷 메서드 전후 모두 실행 */
    @Around("controllerPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        /* 요청별로 식별할 수 있는 고유 식별자 ID 생성 */
        String traceId = UUID.randomUUID().toString().substring(0,8);
        MDC.put("traceId",traceId);


        /* http 요청 정보 가져오기 */
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes())
                .getRequest();

        String uri = request.getRequestURI();
        String httpMethod = request.getMethod();
        String clientIp = request.getRemoteAddr();


        /* 어드바이스가 실행되는 포인트컷 메서드 정보 가져오기 */
        Signature method = joinPoint.getSignature();
        String methodName = method.getName();
        Object[] methodArgs = joinPoint.getArgs();



        // [메서드 실행 전] 웹 정보와 함께 요청 로그 출력
        log.info("[{}] ▶ [REQ] {} {} | IP: {} | Method: {} | Args: {}",
                traceId, httpMethod, uri, clientIp, methodName, Arrays.toString(methodArgs));

        long methodStartTime = System.currentTimeMillis();

        try {
            /* 메서드 로직 실행 */
            Object result = joinPoint.proceed();

            long methodEndTime = System.currentTimeMillis();
            long executionTime = methodEndTime - methodStartTime;

            // [메서드 실행 후] 응답 시간 및 결과 로그 출력
            // kv("executionTime", executionTime) 를 인자로 넘기면 JSON 필드로 독립 생성됩니다.
            log.info("[{}] ◀ [RES] {} {} | Result: {}",
                    traceId, httpMethod, uri, result,
                    kv("executionTime", executionTime)); // 핵심 포인트
            /* 조인포인트 메서드 호출값 return */
            return result;

        }finally {
            MDC.clear();
        }
    }



    /*오류발생시 실행되는 어드바이스*/
    @AfterThrowing(pointcut = "controllerPointcut()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Exception exception){

        String traceId = MDC.get("traceId");

        Signature jp = joinPoint.getSignature();
        String methodName = jp.getName();
        String className = jp.getDeclaringTypeName();

        log.error("[{}] 💥 [ERROR] {} | {} | Message: {}",
                traceId != null ? traceId : "NO-ID", className, methodName, exception.getMessage());
    }
}
