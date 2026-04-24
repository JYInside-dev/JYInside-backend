package org.najeungyoon.communitybackend.model.auth;

public record LoginRequestDTO(
        String username,
        String password
) {
}
