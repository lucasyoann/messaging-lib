package com.marineyoann.messaging.session;

import com.marineyoann.messaging.security.StompPrincipal;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;

@Component
public class ConnectedUserSessionListener {

    private final ConnectedUserRegistry registry;

    public ConnectedUserSessionListener(ConnectedUserRegistry registry) {
        this.registry = registry;
    }

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
        if (accessor != null && accessor.getUser() instanceof StompPrincipal principal) {
            registry.markConnected(principal.userId());
        }
    }

    @EventListener
    public void onDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
        if (accessor != null && accessor.getUser() instanceof StompPrincipal principal) {
            registry.markDisconnected(principal.userId());
        }
    }
}