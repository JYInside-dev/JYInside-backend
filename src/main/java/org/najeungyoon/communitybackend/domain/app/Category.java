package org.najeungyoon.communitybackend.domain.app;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 게시판의 카테고리인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(nullable = false, length = 50)
    private String name;

    // 카테고리 표시 순서
    @Column(nullable = false)
    private int displayOrder = 0;

    // 카테고리 비활성화 여부
    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public Category(Board board, String name, int displayOrder) {
        this.board = board;
        this.name = name;
        this.displayOrder=displayOrder;
        this.active = true;
    }

    public void rename(String name) {
        this.name = name;
    }

    public void changeOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
