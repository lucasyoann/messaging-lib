package com.marineyoann.messaging.notifications.channel;

import com.marineyoann.messaging.notification.NotificationChannel;
import com.marineyoann.messaging.notification.NotificationPayload;
import com.marineyoann.messaging.notifications.push.PushSubscription;
import com.marineyoann.messaging.notifications.push.PushSubscriptionRepository;
import com.marineyoann.messaging.notifications.config.VapidProperties;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;
import java.util.UUID;

@Component
public class WebPushChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(WebPushChannel.class);

    static {
        // <clinit> s'exécute obligatoirement avant le premier <init> de
        // cette classe (garantie du JVM) -- contrairement à un bloc static
        // dans une classe @AutoConfiguration, dont le chargement par Spring
        // (lecture ASM des métadonnées) n'implique pas forcément un vrai
        // chargement JVM de la classe.
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private final PushSubscriptionRepository subscriptionRepository;
    private final PushService pushService;
    private final JsonMapper jsonMapper;

    public WebPushChannel(PushSubscriptionRepository subscriptionRepository,
                          VapidProperties vapidProperties,
                          JsonMapper jsonMapper) throws GeneralSecurityException {
        this.subscriptionRepository = subscriptionRepository;
        this.pushService = new PushService(
                vapidProperties.getPublicKey(), vapidProperties.getPrivateKey(), vapidProperties.getSubject());
        this.jsonMapper = jsonMapper;
    }

    @Override
    public boolean isAvailableFor(UUID userId) {
        return !subscriptionRepository.findByUserId(userId).isEmpty();
    }

    @Override
    public void send(UUID userId, NotificationPayload payload) {
        String json = jsonMapper.writeValueAsString(payload);
        List<PushSubscription> subscriptions = subscriptionRepository.findByUserId(userId);

        for (PushSubscription sub : subscriptions) {
            try {
                Subscription subscription = new Subscription(
                        sub.getEndpoint(),
                        new Subscription.Keys(sub.getP256dhKey(), sub.getAuthKey()));
                Notification notification = new Notification(subscription, json);

                HttpResponse response = pushService.send(notification);
                int status = response.getStatusLine().getStatusCode();

                if (status == 404 || status == 410) {
                    // Abonnement expiré côté navigateur (désinstallation,
                    // changement de navigateur...) : on nettoie plutôt que
                    // de continuer à essayer d'y envoyer des notifications.
                    subscriptionRepository.deleteByEndpoint(sub.getEndpoint());
                    log.info("Abonnement push expiré supprimé pour l'utilisateur {}", userId);
                } else if (status >= 300) {
                    log.warn("Envoi push refusé (HTTP {}) pour l'utilisateur {}", status, userId);
                }
            } catch (Exception e) {
                // Contrat de l'interface : ne jamais laisser une exception
                // remonter, une panne d'un abonnement ne doit pas bloquer
                // les autres ni faire échouer le consumer RabbitMQ appelant.
                log.error("Échec d'envoi push pour l'utilisateur {}", userId, e);
            }
        }
    }
}