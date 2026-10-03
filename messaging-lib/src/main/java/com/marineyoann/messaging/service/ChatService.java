package com.marineyoann.messaging.service;

import com.marineyoann.messaging.domain.Message;
import com.marineyoann.messaging.domain.MessageStatus;
import com.marineyoann.messaging.domain.MessageStatusType;
import com.marineyoann.messaging.domain.OutboxEvent;
import com.marineyoann.messaging.event.MessageCreatedEvent;
import com.marineyoann.messaging.repository.MessageRepository;
import com.marineyoann.messaging.repository.MessageStatusRepository;
import com.marineyoann.messaging.repository.OutboxEventRepository;
import com.marineyoann.messaging.repository.RoomMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private final MessageRepository messageRepository;
    private final MessageStatusRepository statusRepository;
    private final RoomMemberRepository memberRepository;
    private final OutboxEventRepository outboxRepository;
    private final JsonMapper jsonMapper;

    public ChatService(MessageRepository messageRepository,
                       MessageStatusRepository statusRepository,
                       RoomMemberRepository memberRepository,
                       OutboxEventRepository outboxRepository,
                       JsonMapper jsonMapper) {
        this.messageRepository = messageRepository;
        this.statusRepository = statusRepository;
        this.memberRepository = memberRepository;
        this.outboxRepository = outboxRepository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public Message sendMessage(UUID roomId, UUID senderId, String content) {
        // 1. Le message : source de vérité
        Message message = Message.create(roomId, senderId, content);
        messageRepository.save(message);

        // 2. Les destinataires (tous les membres du salon, sauf l'expéditeur)
        List<UUID> recipients = memberRepository.findUserIdsByRoomId(roomId).stream()
                .filter(id -> !id.equals(senderId))
                .toList();

        // 3. Statuts initiaux (SENT) pour chaque destinataire
        for (UUID recipientId : recipients) {
            statusRepository.save(new MessageStatus(message.getId(), recipientId, MessageStatusType.SENT, Instant.now()));
        }

        // 4. L'événement outbox, DANS LA MÊME TRANSACTION que les 3 étapes précédentes
        MessageCreatedEvent event = new MessageCreatedEvent(
                message.getId(), roomId, senderId, content, message.getCreatedAt(), recipients);

        try {
            String json = jsonMapper.writeValueAsString(event);
            outboxRepository.save(new OutboxEvent(UUID.randomUUID(), "MessageCreated", json, Instant.now()));
        } catch (Exception e) {
            // On fait échouer toute la transaction plutôt que de persister un
            // message sans son événement : mieux vaut un envoi qui échoue
            // franchement qu'un message silencieusement jamais diffusé.
            throw new IllegalStateException("Impossible de sérialiser l'événement MessageCreated", e);
        }

        return message;
    }
}
