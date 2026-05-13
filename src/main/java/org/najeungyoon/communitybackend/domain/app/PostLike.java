package org.najeungyoon.communitybackend.domain.app;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.najeungyoon.communitybackend.domain.app.user.User;

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


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = true)
    private User user;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LikeType likeType; // LIKE,DISLIKE


    public PostLike(Post post, LikeType likeType,User user) {
        this.post = post;
        this.likeType = likeType;
        this.user=user;
    }
}
