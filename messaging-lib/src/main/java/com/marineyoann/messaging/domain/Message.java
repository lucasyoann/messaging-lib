package com.marineyoann.messaging.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "message", indexes = {
        @Index(name = "idx_message_room_created", columnList = "roomId, createdAt")
})
public class Message {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID roomId;

    @Column(nullable = false)
    private UUID senderId;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(nullable = false)
    private Instant createdAt;

    protected Message() {
    }

    private Message(UUID id, UUID roomId, UUID senderId, String content, Instant createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = createdAt;
    }

    /**
     * Factory qui centralise la validation métier : peu importe qui crée un
     * message (STOMP, REST, un futur import...), la règle vit à un seul endroit.
     */
    public static Message create(UUID roomId, UUID senderId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Le contenu du message ne peut pas être vide");
        }
        if (content.length() > 4000) {
            throw new IllegalArgumentException("Le message dépasse la taille maximale autorisée (4000 caractères)");
        }
        return new Message(UUID.randomUUID(), roomId, senderId, content, Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
