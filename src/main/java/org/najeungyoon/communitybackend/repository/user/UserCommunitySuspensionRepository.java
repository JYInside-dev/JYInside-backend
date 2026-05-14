package org.najeungyoon.communitybackend.repository.user;

import org.najeungyoon.communitybackend.domain.app.Board;
import org.najeungyoon.communitybackend.domain.app.user.User;
import org.najeungyoon.communitybackend.domain.app.user.UserCommunitySuspension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCommunitySuspensionRepository extends JpaRepository<UserCommunitySuspension,Long> {

    Optional<UserCommunitySuspension> findByUserAndBoard(User user, Board board);
}
