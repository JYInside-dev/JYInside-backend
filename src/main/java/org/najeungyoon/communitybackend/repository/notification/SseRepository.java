package org.najeungyoon.communitybackend.repository.notification;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Optional;

@Repository
public interface SseRepository {
    SseEmitter save(Long userId, SseEmitter sseEmitter);
    void deleteById(Long userId);
    Optional<SseEmitter> findById(Long userId);
}
