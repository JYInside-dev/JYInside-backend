package org.najeungyoon.communitybackend.model;

public record LoginRequestDTO(
        String email,
        String password
) {
}
