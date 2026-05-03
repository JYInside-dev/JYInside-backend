package org.najeungyoon.communitybackend.repository;

import org.najeungyoon.communitybackend.domain.app.Post;
import org.najeungyoon.communitybackend.domain.app.PostLike;
import org.najeungyoon.communitybackend.domain.app.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike,Long> {

    boolean existsByPostAndUser(Post post, User user);
}
