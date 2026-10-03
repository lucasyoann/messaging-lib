package com.marineyoann.messaging.controller;

import com.marineyoann.messaging.domain.MessageStatusType;
import com.marineyoann.messaging.security.StompPrincipal;
import com.marineyoann.messaging.service.MessageStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
public class AckController {

    private static final Logger log = LoggerFactory.getLogger(AckController.class);

    private final MessageStatusService messageStatusService;

    public AckController(MessageStatusService messageStatusService) {
        this.messageStatusService = messageStatusService;
    }

    @MessageMapping("/messages.{messageId}/delivered")
    public void ackDelivered(@DestinationVariable UUID messageId, Principal principal) {
        advance(messageId, principal, MessageStatusType.DELIVERED);
    }

    @MessageMapping("/messages.{messageId}/read")
    public void ackRead(@DestinationVariable UUID messageId, Principal principal) {
        advance(messageId, principal, MessageStatusType.READ);
    }

    private void advance(UUID messageId, Principal principal, MessageStatusType status) {
        UUID userId = ((StompPrincipal) principal).userId();
        messageStatusService.advanceStatus(messageId, userId, status);
    }

    @MessageExceptionHandler(IllegalStateException.class)
    public void handleUnknownMessage(IllegalStateException e) {
        log.warn("Accusé de réception refusé : {}", e.getMessage());
    }
}