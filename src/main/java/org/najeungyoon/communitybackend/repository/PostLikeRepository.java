package org.najeungyoon.communitybackend.repository;

import org.najeungyoon.communitybackend.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike,Long> {

}
