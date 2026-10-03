package com.marineyoann.messaging.security;

import com.marineyoann.messaging.security.exception.StompAuthenticationException;
import com.marineyoann.messaging.security.exception.StompAuthorizationException;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

/**
 * Transforme les exceptions de sécurité STOMP (auth/autorisation) en frames
 * ERROR propres, avec un code stable ("errorCode") exploitable côté client,
 * plutôt que de laisser fuiter le message brut du MessageDeliveryException
 * qui enveloppe l'exception d'origine.
 * <p>
 * Rappel du protocole STOMP : après un frame ERROR, la connexion est
 * fermée par le serveur — ce n'est pas un choix de notre part, c'est
 * la spécification elle-même qui l'impose.
 */
public class MessagingStompErrorHandler extends StompSubProtocolErrorHandler {

    @Override
    protected Message<byte[]> handleInternal(StompHeaderAccessor errorHeaderAccessor,
                                             byte[] errorPayload,
                                             Throwable cause,
                                             StompHeaderAccessor clientHeaderAccessor) {

        String errorCode = "INTERNAL_ERROR";
        String message = "Une erreur est survenue.";

        Throwable authEx = findCause(cause, StompAuthenticationException.class);
        Throwable authzEx = findCause(cause, StompAuthorizationException.class);

        if (authEx != null) {
            errorCode = "AUTH_FAILED";
            message = authEx.getMessage();
        } else if (authzEx != null) {
            errorCode = "FORBIDDEN";
            message = authzEx.getMessage();
        }

        errorHeaderAccessor.setMessage(message);
        errorHeaderAccessor.setNativeHeader("errorCode", errorCode);

        return MessageBuilder.createMessage(errorPayload, errorHeaderAccessor.getMessageHeaders());
    }

    /**
     * Cherche dans toute la chaîne de causes, sans supposer un seul niveau d'enveloppe.
     */
    private static Throwable findCause(Throwable ex, Class<? extends Throwable> type) {
        Throwable current = ex;
        while (current != null) {
            if (type.isInstance(current)) {
                return current;
            }
            current = current.getCause();
        }
        return null;
    }
}
