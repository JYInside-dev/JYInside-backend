package org.najeungyoon.communitybackend.domain.app;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 50)
    private String authorName;

    @Column
    private String password;

    @CreationTimestamp
    @Column(updatable = false, nullable = false) // 생성 시간은 업데이트되지 않도록 막습니다.
    private LocalDateTime createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = true)
    private User user;


    /* 추천 비추천 개수 저장. */
    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false)
    private int disLikeCount = 0;



    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY,cascade = CascadeType.REMOVE,orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();



    /* 익명 사용자용 Post 생성자*/
    public Post(String title, String content, String authorName, String password,Board board) {
        this.title = title;
        this.content = content;
        this.authorName = authorName;
        this.password = password;
        this.board=board;
    }

    /* 로그인된 사용자용 Post 생성자. */
    public Post(String title, String content,User user,Board board) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.board = board;

        this.authorName=user.getNickname();
        this.password="";
    }
}
