package org.najeungyoon.communitybackend.domain.app.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.najeungyoon.communitybackend.domain.app.Board;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
/* 유저의 커뮤니티별 정지 정보 저장 Entity */
public class UserCommunitySuspension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;


    @Column(name = "suspend_until")
    private LocalDateTime suspendUntil;

    @Column(name = "last_suspended_at")
    private LocalDateTime lastSuspendedAt;



    /* 현재 정지상태인지 확인*/
    public boolean isSuspend(){
        return suspendUntil!=null && LocalDateTime.now().isBefore(suspendUntil);
    }

    /* 정지시키는 메서드 */
    public void updateSuspension(int days){
        this.lastSuspendedAt=LocalDateTime.now();
        this.suspendUntil = LocalDateTime.now().plusDays(days);
    }

    /* 정지 해제 메서드 */
    public void liftSuspension(){
        this.suspendUntil=null;
    }



    /* 생성자 */
    public UserCommunitySuspension(User user, Board board) {
        this.user = user;
        this.board = board;
    }
}
