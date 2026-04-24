package org.najeungyoon.communitybackend.service;

import org.najeungyoon.communitybackend.domain.UserRole;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.domain.User;
import org.najeungyoon.communitybackend.jwt.JwtTokenProvider;
import org.najeungyoon.communitybackend.model.login.JwtTokenResponseDTO;
import org.najeungyoon.communitybackend.model.login.LoginRequestDTO;
import org.najeungyoon.communitybackend.model.login.SignupRequestDTO;
import org.najeungyoon.communitybackend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void signup(SignupRequestDTO dto) {
        log.info("회원가입 요청 들어옴 username={}", dto.username());

        validateSignup(dto);

        // 비밀번호 해시 처리
        String encodedPassword = passwordEncoder.encode(dto.password());

        // DB에 들어갈 객체 생성
        User user = new User(
                dto.username(),
                dto.email(),
                encodedPassword,
                dto.nickname(),
                UserRole.USER
        );

        // DB 저장
        userRepository.save(user);

        log.info("회원가입 완료 username={}", dto.username());
    }

    // DB를 읽기만 하고 수정하기 위함 (로그인처럼 조회만 하는 메서드에 사용)
    @Transactional(readOnly = true)
    public JwtTokenResponseDTO login(LoginRequestDTO dto) {
        log.info("로그인 요청 들어옴 username={}", dto.username());

        User user = userRepository.findByUsername(dto.username())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        // 방금 입력한 비밀번호와 DB에 저장된 해시 비밀번호를 matches()로 비교
        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().getKey()
        );

        return new JwtTokenResponseDTO("Bearer", accessToken);
    }


    private void validateSignup(SignupRequestDTO dto) {
        if (dto.username() == null || dto.username().isBlank()) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }

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

        if (userRepository.existsByUsername(dto.username())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByNickname(dto.nickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
    }
}
