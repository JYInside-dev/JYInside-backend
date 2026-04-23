package org.najeungyoon.communitybackend.domain;

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

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Column(nullable = false, length = 20)
    private String role;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createAt;

    public User(String email, String password, String nickname, String role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }
}
