package org.najeungyoon.communitybackend.model.post;

/*
password만 전달하려고 합니다. id는 url로 쏠려고 해요
*/
public record PostDeleteRequestDTO(
        String password
) {
}
