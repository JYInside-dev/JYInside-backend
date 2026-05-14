package org.najeungyoon.communitybackend.domain.app.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.najeungyoon.communitybackend.domain.app.Board;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class) // jpa에게 이벤트 감시하도록 설정
public class UserWarning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" ,nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id" ,nullable = false)
    private Board board;


    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private Long adminId;

    @CreatedDate
    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;


    public UserWarning(User user,Board board, String reason, Long adminId) {
        this.user = user;
        this.board=board;
        this.reason = reason;
        this.adminId = adminId;
    }
}
