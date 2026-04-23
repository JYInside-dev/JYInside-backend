package org.najeungyoon.communitybackend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.domain.User;
import org.najeungyoon.communitybackend.model.SignupRequestDTO;
import org.najeungyoon.communitybackend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequestDTO dto) {
        log.info("회원가입 요청 들어옴 email={}", dto.email());

        validateSignup(dto);

        String encodedPassword = passwordEncoder.encode(dto.password());

        User user = new User(
                dto.email(),
                encodedPassword,
                dto.nickname(),
                "USER"
        );

        userRepository.save(user);

        log.info("회원가입 완료 user email={}", dto.email());
    }

    private void validateSignup(SignupRequestDTO dto) {
        if (dto.email() == null || dto.email().isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        if (dto.password() == null || dto.password().isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }

        if (dto.nickname() == null || dto.nickname().isBlank()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }

        if (dto.password().length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }

        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByNickname(dto.nickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
    }
}
