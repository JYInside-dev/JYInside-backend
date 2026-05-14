package org.najeungyoon.communitybackend.model.notification;

import org.najeungyoon.communitybackend.domain.app.Notification;
import org.najeungyoon.communitybackend.domain.app.NotificationType;
import org.najeungyoon.communitybackend.domain.app.user.User;

public record NotificationEvent(
        User receiver,
        User sender,
        NotificationType type,
        String message,
        String url
) {
}
