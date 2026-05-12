package org.najeungyoon.communitybackend.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.domain.app.Notification;
import org.najeungyoon.communitybackend.model.notification.NotificationEvent;
import org.najeungyoon.communitybackend.repository.notification.NotificationRepository;
import org.najeungyoon.communitybackend.service.NotificationSseService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final NotificationSseService service;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(NotificationEvent event){
        log.info("알림 생성: 타겟 = {}, 발신자 = {}, 타입 = {}",
                event.receiver().getId(),
                event.sender() != null ? event.sender().getId() : "익명",
                event.type());

        try {
            /* 알림 DB에 저장 */
            Notification savedNotification = notificationRepository.save(Notification.from(event));
            /* 알림을  receiver 파이프에 전송 */
            service.sendToClient(event.receiver().getId(), event.type().name(),savedNotification);

        }catch (Exception e){
            log.error("알림 생성중 실패", e);
        }
    }


}
