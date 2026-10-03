package com.marineyoann.messaging.session;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registre des utilisateurs actuellement connectés en WebSocket sur CETTE
 * instance. Utilisé uniquement pour décider si une notification hors ligne
 * doit être envoyée -- ce n'est pas une fonctionnalité de présence visible
 * par les utilisateurs.
 *
 * LIMITE IMPORTANTE : ce registre est en mémoire locale, donc valide
 * uniquement pour l'instance qui l'héberge. Avec plusieurs instances (le
 * scénario de scalabilité horizontale qu'on a posé comme exigence), un
 * utilisateur connecté sur l'instance A apparaîtra "déconnecté" pour
 * l'instance B qui traite l'événement -- risque de notification envoyée
 * à tort à quelqu'un déjà connecté ailleurs. Pas bloquant pour un premier
 * jet (le pire cas est juste une notification superflue), mais à migrer
 * vers un registre partagé (Redis, TTL courte) si ça devient gênant.
 */
@Component
public class ConnectedUserRegistry {

    private final Set<UUID> connectedUserIds = ConcurrentHashMap.newKeySet();

    public void markConnected(UUID userId) {
        connectedUserIds.add(userId);
    }

    public void markDisconnected(UUID userId) {
        connectedUserIds.remove(userId);
    }

    public boolean isConnected(UUID userId) {
        return connectedUserIds.contains(userId);
    }
}