package com.marineyoann.messaging.controller;

import com.marineyoann.messaging.dto.SendMessageRequest;
import com.marineyoann.messaging.security.StompPrincipal;
import com.marineyoann.messaging.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Client envoie vers /app/room.<roomId>/send.
     * L'autorisation (appartenance au salon) est déjà vérifiée en amont
     * par StompAuthChannelInterceptor, qui intercepte aussi les frames SEND
     * -- pas besoin de la revérifier ici.
     */
    @MessageMapping("/room.{roomId}/send")
    public void send(@DestinationVariable UUID roomId, @Payload SendMessageRequest request, Principal principal) {
        UUID senderId = ((StompPrincipal) principal).userId();
        chatService.sendMessage(roomId, senderId, request.content());
    }

    @MessageExceptionHandler(IllegalArgumentException.class)
    public void handleValidationError(IllegalArgumentException e) {
        // Contenu invalide (vide, trop long) : on logue plutôt que de laisser
        // planter la session. Amélioration possible plus tard : notifier le
        // client via /user/queue/errors.
        log.warn("Message rejeté : {}", e.getMessage());
    }
}
