package org.najeungyoon.communitybackend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor

public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 50,unique = true)
    String name;

    @Column(nullable = false,length = 50,unique = true)
    String urlPath;

    @Column(length = 100)
    String description;


    public Board(String name, String urlPath, String description) {
        this.name = name;
        this.urlPath = urlPath;
        this.description = description;
    }
}
