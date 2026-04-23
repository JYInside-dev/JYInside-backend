package org.najeungyoon.communitybackend.repository;

import org.najeungyoon.communitybackend.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {

    List<Post> findByBoard_UrlPath(String urlPath);


    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount+1 where p.id = :postId")
    void incrementLikeCount(@Param("postId") Long postId);
}
