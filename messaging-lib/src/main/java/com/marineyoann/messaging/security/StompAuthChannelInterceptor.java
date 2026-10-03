package com.marineyoann.messaging.security;

import com.marineyoann.messaging.repository.RoomMemberRepository;
import com.marineyoann.messaging.security.exception.StompAuthenticationException;
import com.marineyoann.messaging.security.exception.StompAuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(StompAuthChannelInterceptor.class);
    private static final Pattern ROOM_ID_PATTERN = Pattern.compile("room\\.([0-9a-fA-F-]{36})");

    private final AuthTokenValidator authTokenValidator;
    private final RoomMemberRepository roomMemberRepository;

    public StompAuthChannelInterceptor(AuthTokenValidator authTokenValidator,
                                       RoomMemberRepository roomMemberRepository) {
        this.authTokenValidator = authTokenValidator;
        this.roomMemberRepository = roomMemberRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            return handleConnect(message, accessor);
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand())) {
            handleAuthorization(accessor);
        }

        return message;
    }

    private Message<?> handleConnect(Message<?> message, StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new StompAuthenticationException("Header Authorization manquant ou mal formé au CONNECT");
        }

        String token = authHeader.substring("Bearer ".length());
        AuthTokenValidator.AuthenticatedUser user = authTokenValidator.validate(token)
                .orElseThrow(() -> new StompAuthenticationException("Token invalide ou expiré"));

        accessor.setUser(new StompPrincipal(user.userId(), user.displayName()));
        log.debug("Session STOMP authentifiée pour l'utilisateur {}", user.userId());
        return message;
    }

    private void handleAuthorization(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof StompPrincipal principal)) {
            throw new StompAuthenticationException("Aucune session authentifiée pour cette frame");
        }

        Optional<UUID> roomId = extractRoomId(accessor.getDestination());
        if (roomId.isEmpty()) {
            return; // destination ne ciblant pas un salon précis, rien à vérifier ici
        }

        if (!roomMemberRepository.existsByRoomIdAndUserId(roomId.get(), principal.userId())) {
            log.warn("Utilisateur {} refusé sur le salon {} (non membre)", principal.userId(), roomId.get());
            throw new StompAuthorizationException("Vous n'êtes pas membre de ce salon");
        }
    }

    private Optional<UUID> extractRoomId(String destination) {
        if (destination == null) return Optional.empty();
        Matcher matcher = ROOM_ID_PATTERN.matcher(destination);
        if (matcher.find()) {
            try {
                return Optional.of(UUID.fromString(matcher.group(1)));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
