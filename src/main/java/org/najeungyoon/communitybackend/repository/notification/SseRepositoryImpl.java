package org.najeungyoon.communitybackend.repository.notification;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SseRepositoryImpl implements SseRepository{

    /* 멀티스레드 환경에서 유저별로 만들어지는 각 스레드의 emitter(발사파이프)를 저장하는 해쉬맵 */
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter save(Long userId, SseEmitter sseEmitter) {
        emitters.put(userId, sseEmitter);
        return sseEmitter;
    }

    @Override
    public void deleteById(Long userId) {
        emitters.remove(userId);
    }

    @Override
    public Optional<SseEmitter> findById(Long userId) {
        return Optional.ofNullable(emitters.get(userId));
    }

    @Override
    public Map<Long, SseEmitter> findAllEmitters() {
        return Collections.unmodifiableMap(emitters);
    }
}
