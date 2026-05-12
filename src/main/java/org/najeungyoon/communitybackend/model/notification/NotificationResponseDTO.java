package org.najeungyoon.communitybackend.model.notification;

import org.najeungyoon.communitybackend.domain.app.Notification;

import java.time.LocalDateTime;

public record NotificationResponseDTO(
        Long id,
        String message,
        String url,
        boolean isRead,
        LocalDateTime createdAt
) {

    public static NotificationResponseDTO from (Notification notification){
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getMessage(),
                notification.getUrl(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
