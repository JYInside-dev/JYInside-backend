package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.domain.app.user.User;
import org.najeungyoon.communitybackend.model.user.UserMeResponseDTO;
import org.najeungyoon.communitybackend.repository.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserMeResponseDTO getMe(Long userId) {
        User user = findUser(userId);

        return new UserMeResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getPassword() == null || user.getPassword().isBlank()
        );
    }

    @Transactional
    public void changeNickname(Long userId, String newNickname) {
        User user = findUser(userId);

        String trimmedNickname = newNickname.trim();

        /* 새 닉네임이 원래 닉네임이랑 같으면 리턴 */
        if (trimmedNickname.equals(user.getNickname())) {
            return;
        }

        /* 이미 있는 닉네임이면 안된다 리턴 */
        userRepository.findByNickname(trimmedNickname)
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(userId)) {
                        throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
                    }
                });

        user.changeNickname(trimmedNickname);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findUser(userId);

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("소셜 로그인 계정은 비밀번호를 변경할 수 없습니다.");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodedPassword);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    }
}
