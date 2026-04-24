package org.najeungyoon.communitybackend.model.login;

public record LoginRequestDTO(
        String username,
        String password
) {
}
