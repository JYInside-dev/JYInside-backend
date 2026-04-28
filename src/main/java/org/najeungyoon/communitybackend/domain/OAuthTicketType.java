package org.najeungyoon.communitybackend.domain;

// 티켓의 용도 구분
public enum OAuthTicketType {
    // 기존 가입자가 소셜 로그인 성공 후 JWT로 교환할 티켓
    LOGIN,
    // 신규 소셜 가입자가 추가 정보를 입력할 때 쓰는 티켓
    SOCIAL_SIGNUP
}
