package org.najeungyoon.communitybackend.event;

import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.domain.app.Notification;
import org.najeungyoon.communitybackend.domain.app.NotificationType;
import org.najeungyoon.communitybackend.domain.app.user.User;
import org.najeungyoon.communitybackend.model.notification.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    /* 이벤트 발생 객체 */
    private final ApplicationEventPublisher eventPublisher;


    public void publishEvent(User receiver, User sender, NotificationType type,
                             String message, String targetUrl){

        /* 나->나로 가는 알림이 아닌 경우 종료 */
        if (receiver == null || (sender != null && receiver.getId().equals(sender.getId()))) {
            return;
        }

        NotificationEvent notificationEvent = new NotificationEvent(
                receiver,  // 수신자 (게시글 작성자)
                sender,    // 발신자 (익명이면 null이 안전하게 들어감)
                type,
                message,
                targetUrl
        );

        eventPublisher.publishEvent(notificationEvent);


    }

}
