package org.najeungyoon.communitybackend.domain.app;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.najeungyoon.communitybackend.domain.app.user.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

 /*   @Column(nullable = false)
    private Long userId; */

    @Column(nullable = false, length = 100)
    private String content;

    @Column(nullable = false,length = 50)
    private String authorName;

    @Column
    private String password;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "post_id",nullable = false)
    private Post post;


    @Column(length = 50)
    private String mentionedName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;


    @OneToMany(fetch = FetchType.LAZY,mappedBy = "parent",cascade = CascadeType.ALL, orphanRemoval = true)
    @Column
    private List<Comment> children= new ArrayList<>();



    /* 댓글 내용 수정/업데이트 */
    public void update(String content){
        this.content=content;
    }

    /* 비회원용 댓글 생성자
    *   authorName, password 존재함. */
    public Comment(String content, String authorName, String password, Post post,String mentionedName,Comment parent) {
        this.content = content;
        this.authorName = authorName;
        this.password = password;
        this.post = post;
        this.mentionedName = mentionedName;
        this.parent=parent;
    };


    /* 로그인용 댓글 생성자 */
    public Comment(String content, User user, Post post,String mentionedName,Comment parent) {
        this.content = content;
        this.authorName = user.getNickname();
        this.user=user;
        this.password = "";
        this.post = post;
        this.mentionedName = mentionedName;
        this.parent=parent;
    };


}
