package org.najeungyoon.communitybackend.domain.app;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.najeungyoon.communitybackend.domain.app.user.User;
import org.najeungyoon.communitybackend.model.notification.NotificationEvent;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Notification {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id",nullable = true)
    private User sender;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;


    /* 알림과 함께 같이 갈 메세지 */
    @Column(nullable = false, length = 100)
    private String message;


    /* 알림을 누르면 이동할 URL */
    @Column(nullable = true)
    private String url;


    /* 알림 읽음 여부 */
    @Column(nullable = false)
    private boolean isRead;


    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    public Notification(User receiver, User sender, NotificationType notificationType, String message, String url) {
        this.receiver = receiver;
        this.sender = sender;
        this.notificationType = notificationType;
        this.message = message;
        this.url = url;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }


    /* 읽음 처리 */
    public void setRead(){
        this.isRead=true;
    }


    public static Notification from(NotificationEvent event){
        return new Notification(
                event.receiver(),
                event.sender(),
                event.type(),
                event.message(),
                event.url()
        );
    }

}
