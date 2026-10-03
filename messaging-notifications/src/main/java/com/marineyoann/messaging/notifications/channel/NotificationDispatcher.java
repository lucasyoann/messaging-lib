package com.marineyoann.messaging.notifications.channel;

import com.marineyoann.messaging.notification.NotificationChannel;
import com.marineyoann.messaging.notification.NotificationPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final List<NotificationChannel> channels;

    public NotificationDispatcher(List<NotificationChannel> channels) {
        this.channels = channels;
    }

    public void dispatch(UUID userId, NotificationPayload payload) {
        channels.stream()
                .filter(c -> c.isAvailableFor(userId))
                .findFirst()
                .ifPresentOrElse(
                        c -> c.send(userId, payload),
                        () -> log.debug("Aucun canal de notification disponible pour l'utilisateur {}", userId)
                );
    }
}