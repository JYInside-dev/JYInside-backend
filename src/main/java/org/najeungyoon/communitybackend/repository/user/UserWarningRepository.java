package org.najeungyoon.communitybackend.repository.user;

import org.najeungyoon.communitybackend.domain.app.Board;
import org.najeungyoon.communitybackend.domain.app.user.User;
import org.najeungyoon.communitybackend.domain.app.user.UserWarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserWarningRepository extends JpaRepository<UserWarning,Long> {


    /* 단순 조회 */
    long countByUser(User user);

    long countByUserAndBoard(User user, Board board);
    
    long countByUserAndBoardAndCreatedAtAfter(User user, Board board, LocalDateTime createdAtAfter);
}
