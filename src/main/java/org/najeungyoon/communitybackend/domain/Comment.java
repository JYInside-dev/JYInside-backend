package org.najeungyoon.communitybackend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String content;

    @Column(nullable = false,length = 50)
    private String authorName;

    @Column
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
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




    public Comment(String content, String authorName, String password, Post post,String mentionedName,Comment parent) {
        this.content = content;
        this.authorName = authorName;
        this.password = password;
        this.post = post;
        this.mentionedName = mentionedName;
        this.parent=parent;
    };
}
