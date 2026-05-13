package org.najeungyoon.communitybackend.service;

import org.najeungyoon.communitybackend.domain.app.user.UserRole;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.domain.app.user.User;
import org.najeungyoon.communitybackend.jwt.JwtTokenProvider;
import org.najeungyoon.communitybackend.model.auth.JwtTokenResponseDTO;
import org.najeungyoon.communitybackend.model.auth.LoginRequestDTO;
import org.najeungyoon.communitybackend.model.auth.SignupRequestDTO;
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

        /* 이미 존재하는 id,pw,email 입력시 예외처리*/
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

        // 소셜 가입자는 비밀번호가 없을 수 있기 때문에 추가
        if (user.getPassword() == null) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().getKey() // ROLE_* 형식의 문자열을 인자로 전송
        );

        return new JwtTokenResponseDTO(
                "Bearer",
                accessToken,
                user.getUsername(),
                user.getNickname()
        );
    }


    /* 형식오류나 빈칸시에는 ControllerAdvice가 대신 처리함
    *  중복validate는 service에서 자체 처리 */
    private void validateSignup(SignupRequestDTO dto) {

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
