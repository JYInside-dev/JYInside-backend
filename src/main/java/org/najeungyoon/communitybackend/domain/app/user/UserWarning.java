package org.najeungyoon.communitybackend.domain.app.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class UserWarning {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" ,nullable = false)
    private User user;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private Long adminId;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;


}
