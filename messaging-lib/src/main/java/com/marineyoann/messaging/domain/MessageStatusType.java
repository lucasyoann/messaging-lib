package com.marineyoann.messaging.domain;

/**
 * Statut de livraison d'un message, du point de vue d'un destinataire précis.
 * L'ordre naturel (SENT < DELIVERED < READ) est utilisé pour éviter qu'un ACK
 * tardif ne fasse régresser un statut déjà plus avancé (voir MessageStatus#advanceTo).
 */
public enum MessageStatusType {
    SENT(0),
    DELIVERED(1),
    READ(2);

    private final int rank;

    MessageStatusType(int rank) {
        this.rank = rank;
    }

    public boolean isMoreAdvancedThan(MessageStatusType other) {
        return this.rank > other.rank;
    }
}
