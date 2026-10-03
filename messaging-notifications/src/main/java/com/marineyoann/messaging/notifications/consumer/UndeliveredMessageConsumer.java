package com.marineyoann.messaging.notifications.consumer;

import com.marineyoann.messaging.event.MessageCreatedEvent;
import com.marineyoann.messaging.notification.NotificationPayload;
import com.marineyoann.messaging.notifications.channel.NotificationDispatcher;
import com.marineyoann.messaging.session.ConnectedUserRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import static com.marineyoann.messaging.notifications.config.NotificationsRabbitConfig.NOTIFICATIONS_QUEUE;

@Component
public class UndeliveredMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(UndeliveredMessageConsumer.class);

    private final ConnectedUserRegistry connectedUserRegistry;
    private final NotificationDispatcher dispatcher;
    private final JsonMapper jsonMapper;

    public UndeliveredMessageConsumer(ConnectedUserRegistry connectedUserRegistry,
                                      NotificationDispatcher dispatcher,
                                      JsonMapper jsonMapper) {
        this.connectedUserRegistry = connectedUserRegistry;
        this.dispatcher = dispatcher;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener(queues = NOTIFICATIONS_QUEUE)
    public void handleMessageCreated(byte[] payload) {
        MessageCreatedEvent event;
        try {
            event = jsonMapper.readValue(payload, MessageCreatedEvent.class);
        } catch (Exception e) {
            log.error("Impossible de désérialiser un MessageCreatedEvent, événement ignoré", e);
            return;
        }

        NotificationPayload notification = new NotificationPayload(
                "Nouveau message",
                event.content(),
                "/rooms/" + event.roomId(),
                event.messageId(),
                event.roomId()
        );

        for (var recipientId : event.recipientIds()) {
            if (!connectedUserRegistry.isConnected(recipientId)) {
                dispatcher.dispatch(recipientId, notification);
            }
        }
    }
}