package org.najeungyoon.communitybackend.repository.notification;

import org.najeungyoon.communitybackend.domain.app.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {

    public List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long userId);
}
