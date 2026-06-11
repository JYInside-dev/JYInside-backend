package org.najeungyoon.communitybackend.domain.app.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column()
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;


    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createAt;


    /* util 메서드 */
    public void changeRole(UserRole role){
        this.role=role;
    }

    /* 닉네임 변경 메서드 */
    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    /* 비밀번호 변경 메서드 */
    public void changePassword(String password) {
        this.password = password;
    }


    /* 생성자 */
    public User(String username, String email, String password, String nickname, UserRole role,UserStatus userStatus) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.status=userStatus;
    }
}
