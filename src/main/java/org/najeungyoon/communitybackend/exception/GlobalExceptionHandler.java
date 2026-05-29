package org.najeungyoon.communitybackend.exception;

import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.model.error.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler{

    /* 메서드에 인자가 잘못들어가거나 안들어갔을 때 발생하는 예외처리*/
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e){

        String errorMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        log.error("Validation Error: {}", errorMessage);

        return ResponseEntity.status(400).body(new ErrorResponse(400,errorMessage,"INVALID_INPUT"));
    }

    /* 서비스 로직에서 던져지는 예외들 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e){
        log.error("Business Error: {}",e.getMessage());
        return ResponseEntity.status(400).body(new ErrorResponse(400,e.getMessage(),"BAD_REQUEST"));
    }


    /* Authorization(인가) 예외처리 */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e){
        log.error("Authorization Error: {}",e.getMessage());
        return ResponseEntity.status(403).body(new ErrorResponse(403,e.getMessage(),"Forbidden"));
    }

    /* Spring Security - 인증 실패 예외 (401) */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        log.error("Authentication Error: {}", e.getMessage());
        return ResponseEntity.status(401).body(new ErrorResponse(401, "로그인이 필요하거나 토큰이 만료되었습니다.", "UNAUTHORIZED"));
    }

    /* Spring Security - 인가(권한) 실패 예외 (403) */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        String errorMessage = "Access Denied".equals(e.getMessage())
                ? "해당 기능에 대한 접근 권한이 없습니다."
                : e.getMessage();

        log.error("Access Denied Error: {}", errorMessage);
        return ResponseEntity.status(403).body(new ErrorResponse(403, errorMessage, "FORBIDDEN"));
    }



    // 그 외 예상치 나머지 모든 서버 에러 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception e) {
        log.error("Unexpected Error: ", e);
        return ResponseEntity.status(500).body(new ErrorResponse(500, "서버 내부 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR"));
    }

}
