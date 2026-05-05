package org.najeungyoon.communitybackend.domain.app;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(
        name = "board_admins",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "board_id"})
        }
)
public class BoardAdmin {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;


        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id",nullable = false)
        private User user;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "board_id",nullable = false)
        private Board board;


        public BoardAdmin(User user, Board board) {
                this.user = user;
                this.board = board;
        }
}
