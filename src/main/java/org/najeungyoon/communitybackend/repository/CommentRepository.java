package org.najeungyoon.communitybackend.repository;

import org.najeungyoon.communitybackend.domain.app.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByPostIdAndParentIsNull(Long postId);
}
