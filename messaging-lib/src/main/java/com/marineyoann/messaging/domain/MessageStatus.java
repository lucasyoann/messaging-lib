package com.marineyoann.messaging.domain;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Statut d'un message pour un destinataire donné. Une ligne par (message, utilisateur).
 * C'est ce qui alimente les accusés de réception (delivered / read).
 */
@Entity
@Table(name = "message_status")
@IdClass(MessageStatus.MessageStatusId.class)
public class MessageStatus {

    @Id
    @Column(name = "message_id")
    private UUID messageId;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageStatusType status;

    @Column(nullable = false)
    private Instant updatedAt;

    /** Verrou optimiste : évite qu'un ACK "delivered" tardif écrase un "read" plus récent en cas de concurrence. */
    @Version
    private long version;

    protected MessageStatus() {
    }

    public MessageStatus(UUID messageId, UUID userId, MessageStatusType status, Instant updatedAt) {
        this.messageId = messageId;
        this.userId = userId;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    /**
     * Fait progresser le statut, en ignorant toute régression (ex: un
     * DELIVERED qui arrive après un READ déjà enregistré).
     */
    public void advanceTo(MessageStatusType newStatus) {
        if (!newStatus.isMoreAdvancedThan(this.status) && newStatus != this.status) {
            return;
        }
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public UUID getMessageId() {
        return messageId;
    }

    public UUID getUserId() {
        return userId;
    }

    public MessageStatusType getStatus() {
        return status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public static class MessageStatusId implements Serializable {
        private UUID messageId;
        private UUID userId;

        public MessageStatusId() {
        }

        public MessageStatusId(UUID messageId, UUID userId) {
            this.messageId = messageId;
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MessageStatusId that)) return false;
            return Objects.equals(messageId, that.messageId) && Objects.equals(userId, that.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(messageId, userId);
        }
    }
}
