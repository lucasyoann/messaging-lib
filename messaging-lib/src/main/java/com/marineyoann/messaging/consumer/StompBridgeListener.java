package com.marineyoann.messaging.consumer;

import com.marineyoann.messaging.event.MessageCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import static com.marineyoann.messaging.config.RabbitMqConfig.STOMP_BRIDGE_QUEUE;

/**
 * Le pont entre la fiabilité (outbox/RabbitMQ) et le temps réel (STOMP).
 * Consomme les événements MessageCreated et les diffuse aux clients
 * abonnés au salon concerné.
 */
@Component
public class StompBridgeListener {

    private static final Logger log = LoggerFactory.getLogger(StompBridgeListener.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final JsonMapper jsonMapper;

    public StompBridgeListener(SimpMessagingTemplate messagingTemplate, JsonMapper jsonMapper) {
        this.messagingTemplate = messagingTemplate;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener(queues = STOMP_BRIDGE_QUEUE)
    public void handleMessageCreated(byte[] payload) {
        MessageCreatedEvent event;
        try {
            event = jsonMapper.readValue(payload, MessageCreatedEvent.class);
        } catch (Exception e) {
            // Un événement illisible ne doit jamais faire planter le listener :
            // on le logue et on l'ignore plutôt que de bloquer toute la queue.
            log.error("Impossible de désérialiser un MessageCreatedEvent, événement ignoré", e);
            return;
        }

        String destination = "/topic/room." + event.roomId();
        messagingTemplate.convertAndSend(destination, event);
        log.debug("Message {} diffusé sur {}", event.messageId(), destination);
    }
}
