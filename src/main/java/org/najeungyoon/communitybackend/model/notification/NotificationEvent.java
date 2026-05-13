package org.najeungyoon.communitybackend.model.notification;

import org.najeungyoon.communitybackend.domain.app.Notification;
import org.najeungyoon.communitybackend.domain.app.user.User;

public record NotificationEvent(
        User receiver,
        User sender,
        Notification.NotificationType type,
        String message,
        String url
) {
}
