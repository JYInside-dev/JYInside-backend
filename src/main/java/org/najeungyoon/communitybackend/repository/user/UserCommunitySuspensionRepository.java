package org.najeungyoon.communitybackend.repository.user;

import jakarta.persistence.Entity;
import org.najeungyoon.communitybackend.domain.app.Board;
import org.najeungyoon.communitybackend.domain.app.user.User;
import org.najeungyoon.communitybackend.domain.app.user.UserCommunitySuspension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCommunitySuspensionRepository extends JpaRepository<UserCommunitySuspension,Long> {

    Optional<UserCommunitySuspension> findByUserAndBoard(User user, Board board);

    Optional<UserCommunitySuspension> findByUser_IdAndBoard_UrlPath (Long userId,String urlPath);



    /* urlPath 게시판에 정지되어 있는 유저들 호출 */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT s FROM UserCommunitySuspension s " +
            "WHERE s.board.urlPath = :urlPath " +
            "AND s.suspendUntil IS NOT NULL " +
            "AND s.suspendUntil > CURRENT_TIMESTAMP")
    Page<UserCommunitySuspension> findSuspendedUserByBoard_UrlPath(@Param("urlPath") String urlPath, Pageable pageable);

}
