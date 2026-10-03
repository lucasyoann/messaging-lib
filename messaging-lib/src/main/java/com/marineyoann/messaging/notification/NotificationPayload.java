package com.marineyoann.messaging.notification;

import java.util.UUID;

public record NotificationPayload(
        String title,
        String body,
        String deepLinkUrl,
        UUID messageId,
        UUID roomId
) {
}