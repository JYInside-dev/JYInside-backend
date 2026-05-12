package org.najeungyoon.communitybackend.controller;

import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.model.notification.NotificationResponseDTO;
import org.najeungyoon.communitybackend.service.NotificationSseService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationSseService notificationSseService;


    /* 모든 알림 불러오기 */
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getAllNotifications(
            @AuthenticationPrincipal Long userId
    ){
        return  ResponseEntity.ok(notificationSseService.getAllNotifications(userId));
    }


    /* 알림 읽음 처리 API */
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable("id") Long id) {
        notificationSseService.markAsRead(id);
        return ResponseEntity.ok().build();
    }


    /* 로그인 시 Emitter 파이프 통로 하나 개통 */
    @GetMapping(value = "/subscribe",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(
        @AuthenticationPrincipal Long userId
    ){
        SseEmitter sseEmitter = notificationSseService.subScribe(userId);

        // Nginx 등 리버스 프록시 환경에서 버퍼링을 비활성화하는 헤더 추가
        return ResponseEntity.ok()
                .header("X-Accel-Buffering", "no")
                .body(sseEmitter);
    }
}
