package org.najeungyoon.communitybackend.model.grant;


public record GrantCommunityAdminRequestDTO(
        Long userId,
        String boardUrlPath
) {
}
