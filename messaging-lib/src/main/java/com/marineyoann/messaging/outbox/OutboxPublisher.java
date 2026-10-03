package com.marineyoann.messaging.outbox;

import com.marineyoann.messaging.config.RabbitMqConfig;
import com.marineyoann.messaging.domain.OutboxEvent;
import com.marineyoann.messaging.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private static final int BATCH_SIZE = 100;

    private final OutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxPublisher(OutboxEventRepository outboxRepository, RabbitTemplate rabbitTemplate) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> batch = outboxRepository.findBatchToPublish(PageRequest.of(0, BATCH_SIZE));
        if (batch.isEmpty()) {
            return;
        }

        for (OutboxEvent event : batch) {
            try {
                Message amqpMessage = MessageBuilder
                        .withBody(event.getPayload().getBytes(StandardCharsets.UTF_8))
                        .setContentType("application/json")
                        .setHeader("eventType", event.getEventType())
                        .setMessageId(event.getId().toString()) // clé d'idempotence côté consommateur
                        .build();

                rabbitTemplate.send(RabbitMqConfig.MESSAGING_EXCHANGE, event.getEventType(), amqpMessage);
                event.markPublished();
            } catch (Exception e) {
                event.incrementAttempts();
                log.warn("Échec de publication de l'événement outbox {} (tentative {}) : {}",
                        event.getId(), event.getAttempts(), e.getMessage());
            }
        }
        outboxRepository.saveAll(batch);
    }
}
