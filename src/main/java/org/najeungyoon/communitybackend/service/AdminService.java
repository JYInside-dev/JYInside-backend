package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.domain.User;
import org.najeungyoon.communitybackend.domain.UserRole;
import org.najeungyoon.communitybackend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    /* 기존 유저의 권한 변경 */
    @Transactional
    public void updateUserRole(String userName, UserRole role){
        User targetUser = userRepository.findByUsername(userName)
                .orElseThrow(()->new IllegalArgumentException("해당 유저를 찾을 수 없습니다. "));

        targetUser.changeRole(role);
    }

}
