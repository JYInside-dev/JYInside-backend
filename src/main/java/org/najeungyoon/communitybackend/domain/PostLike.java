package org.najeungyoon.communitybackend.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(
        /* 추천수 주작 방지를 위한 회원 id별 제약조건 추가 */
        name = "post_like",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "user_id",
                                "post_id"
                        }
                )
        }
)
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id",nullable = false)
    private Post post;

    /* TODO
    *   JWT로그인이 구현되면 체크할 ID
    *   현재는 null허용함. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LikeType likeType; // LIKE,DISLIKE

    /* TODO
    *   생성자에 memberId 추가 */
    public PostLike(Post post, LikeType likeType,User user) {
        this.post = post;
        this.likeType = likeType;
        this.user=user;
    }
}
