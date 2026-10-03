package com.marineyoann.messaging.notification;

import java.util.UUID;

/**
 * Abstraction d'un canal de notification hors ligne (web push aujourd'hui,
 * email possible demain). Toute nouvelle implémentation est détectée
 * automatiquement par Spring (voir NotificationDispatcher) sans modifier
 * le code existant.
 *
 * Contrat important : une implémentation ne doit JAMAIS laisser une
 * exception remonter depuis send() — une panne d'un canal ne doit pas
 * empêcher les autres de fonctionner.
 */
public interface NotificationChannel {

    boolean isAvailableFor(UUID userId);

    void send(UUID userId, NotificationPayload payload);
}