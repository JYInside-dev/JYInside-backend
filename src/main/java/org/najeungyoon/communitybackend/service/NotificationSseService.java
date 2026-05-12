package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.domain.app.Notification;
import org.najeungyoon.communitybackend.model.notification.NotificationResponseDTO;
import org.najeungyoon.communitybackend.repository.notification.NotificationRepository;
import org.najeungyoon.communitybackend.repository.notification.SseRepositoryImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSseService {

    /* Sse 파이프가 존재하는 시간 */
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private final SseRepositoryImpl sseRepository;
    private final NotificationRepository notificationRepository;


    /* 클라이언트가 SSE통신을 요구할 때 이 메서드가 호출됨 */
    public SseEmitter subScribe(Long userId){

        /* 던져질 SseEmitter 객체 */
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        sseRepository.save(userId,emitter);

        log.info("SSE 구독 완료 (파이프 생성 완료) : User ID = {}", userId);

        // Emitter의 상태에 따른 콜백 설정 (네트워크 끊김, 타임아웃, 에러 시 메모리에서 즉시 제거)
        emitter.onCompletion(() -> sseRepository.deleteById(userId));
        emitter.onTimeout(() -> sseRepository.deleteById(userId));
        emitter.onError((e) -> sseRepository.deleteById(userId));

        // 503 Service Unavailable 방지용 더미 데이터 전송
        // 클라이언트가 연결되자마자 아무 데이터도 받지 못하면 브라우저가 에러로 간주할 수 있음.
        sendToClient(userId, "subscribe", "EventStream Created. [userId=" + userId + "]");

        return emitter;
    }


    /* 파이프가 연결된 클라이언트에게 실제 알림 전송 */
    public void sendToClient(Long userId, String eventName, Object data){
        /* ifPresent -> 값이 존재하면 람다식 수행 */
        sseRepository.findById(userId).ifPresent(emitter -> {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                sseRepository.deleteById(userId);
                log.error("SSE 전송 오류", e);
            }
        });
    }


    /* DB에서 모든 알림 가져오기 */
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getAllNotifications(Long userId){
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponseDTO::from)
                .toList();
    }


    /* 읽음 처리 */
    @Transactional
    public void markAsRead(Long notificationId) {
        // Notification 엔티티에 만들어둔 setRead() 호출
        notificationRepository.findById(notificationId)
                .ifPresent(Notification::setRead);
    }

}
