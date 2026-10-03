package com.marineyoann.messaging.service;

import com.marineyoann.messaging.domain.MessageStatus;
import com.marineyoann.messaging.domain.MessageStatusType;
import com.marineyoann.messaging.repository.MessageStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MessageStatusService {

    private final MessageStatusRepository repository;

    public MessageStatusService(MessageStatusRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void advanceStatus(UUID messageId, UUID userId, MessageStatusType newStatus) {
        MessageStatus status = repository.findByMessageIdAndUserId(messageId, userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Aucun statut initial pour ce message/utilisateur : " + messageId + "/" + userId));
        status.advanceTo(newStatus); // logique anti-régression déjà dans l'entité
        // pas besoin de repository.save() ici : status est "managed" par JPA
        // dans le contexte de persistance de cette transaction (dirty checking)
    }

    public List<MessageStatus> findByMessageId(UUID messageId) {
        return repository.findByMessageId(messageId);
    }
}
