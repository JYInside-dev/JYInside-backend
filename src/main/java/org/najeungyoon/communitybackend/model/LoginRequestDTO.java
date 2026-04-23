package org.najeungyoon.communitybackend.model;

public record LoginRequestDTO(
        String username,
        String password
) {
}
