package org.najeungyoon.communitybackend.domain.app.user;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {


    SUPER_ADMIN("ROLE_SUPER_ADMIN","전체 관리자"),
    COMMUNITY_ADMIN("ROLE_COMMUNITY_ADMIN","커뮤니티 관리자"),
    USER("ROLE_USER","일반 사용자");

    private final String key;
    private final String title;

}
