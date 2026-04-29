package org.najeungyoon.communitybackend.model.error;


/* 예외가 터졌을 때 프론트엔드로 보낼 에러 응답 DTO*/
public record ErrorResponse(
        int status, // HTTP 코드 (402,404)등
        String message, // 프론트엔드에 무슨 상황인지 알릴 메세지
        String code // 서버 내부 식별 코드
){ }
