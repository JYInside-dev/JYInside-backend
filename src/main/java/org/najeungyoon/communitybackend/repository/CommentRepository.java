package org.najeungyoon.communitybackend.repository;

import org.najeungyoon.communitybackend.domain.app.Comment;
import org.najeungyoon.communitybackend.domain.app.user.User;
import org.najeungyoon.communitybackend.model.post.PostCommentCountDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    Page<Comment> findByPostIdAndParentIsNullOrderByIdAsc(Long postId, Pageable pageable);

    List<Comment> findAllByUser(User user);

    /* Join으로 Comment Post Board 한꺼번에 가져와서 N+1 해결하는 쿼리 */
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.post p" +
            " JOIN FETCH p.board b" +
            " where c.user.id = :userId")
    List<Comment> findAllByUserIdWithPostAndBoard(@Param("userId") Long userId);


    @Query("""
    SELECT new org.najeungyoon.communitybackend.model.post.PostCommentCountDTO(
        c.post.id,
        COUNT(c.id)
    )
    FROM Comment c
    WHERE c.post.id IN :postIds
    GROUP BY c.post.id
""")
    List<PostCommentCountDTO> countByPostIds(
            @Param("postIds") List<Long> postIds
    );

}
