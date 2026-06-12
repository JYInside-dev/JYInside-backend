package org.najeungyoon.communitybackend.repository;

import org.najeungyoon.communitybackend.domain.app.Board;
import org.najeungyoon.communitybackend.domain.app.BoardAdmin;
import org.najeungyoon.communitybackend.domain.app.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardAdminRepository extends JpaRepository<BoardAdmin,Long> {

    public boolean existsByUserAndBoard(User user, Board board);
    boolean existsByUser_IdAndBoard_UrlPath(Long userId, String urlPath);


    /* 모든 어드민유저들 호출 */
    @Query("SELECT ba FROM BoardAdmin ba JOIN FETCH ba.user JOIN FETCH ba.board")
    List<BoardAdmin> findAllWithUserAndBoard();

    Optional<BoardAdmin> findByUser_IdAndBoard_UrlPath(Long userId, String urlPath);

    // 💡 잔여 권한 확인을 위한 count 메소드도 동일한 규칙으로 작성 가능
    long countByUser_Id(Long userId);
}
