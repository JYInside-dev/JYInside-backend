package org.najeungyoon.communitybackend.repository;

import org.najeungyoon.communitybackend.domain.app.Board;
import org.najeungyoon.communitybackend.domain.app.Post;
import org.najeungyoon.communitybackend.domain.app.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {



    List<Post> findByBoard_UrlPath(String urlPath);

    List<Post> findAllByUser(User user);


    /* 추천수 증가 */
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount+1 where p.id = :postId")
    void incrementLikeCount(@Param("postId") Long postId);

    /* threshold 이상은 추천수를 가진 글들을 조회
    *  TODO
    *   로컬에서 index를 만들어서 조회가 빠른데
    *   나중에 도커나 클라우드 환경에서도 index를 직접 만들어줘야함. */
    List<Post> findByLikeCountGreaterThanEqualOrderByCreateAtDesc(int threshold);

    /* 메인화면에서 인기글 5개 목록 보여줌.*/
    List<Post> findTop5ByLikeCountGreaterThanEqualOrderByCreateAtDesc(int threshold);

    /* 제목 검색 */
    List<Post> findByBoardAndTitleContainingIgnoreCase(Board board, String keyword);

    /* 내용 검색 */
    List<Post> findByBoardAndContentContainingIgnoreCase(Board board, String keyword);

    /* 글쓴이 검색 */
    List<Post> findByBoardAndAuthorNameContainingIgnoreCase(Board board, String keyword);

    /* 전체 검색 */
    @Query("""
        SELECT p
        FROM Post p
        WHERE p.board = :board
            AND (
                LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.authorName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
        ORDER BY p.createAt DESC
    """)
    List<Post> searchAllByBoard(
            @Param("board") Board board,
            @Param("keyword") String keyword
    );




    /* 게시글(Post)& 작성자(User)
        둘이 한꺼번에 가져오는 쿼리 */
    @Query("select p from Post p join fetch p.board left join p.user where p.id = :postId")
    Optional<Post> findByIdWithBoardAndUser(@Param("postId") Long postId);

}
