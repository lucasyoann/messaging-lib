package com.marineyoann.messaging.controller;

import com.marineyoann.messaging.domain.Message;
import com.marineyoann.messaging.dto.MessageDto;
import com.marineyoann.messaging.repository.MessageRepository;
import com.marineyoann.messaging.repository.RoomMemberRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/api/rooms/{roomId}/messages")
public class MessageHistoryController {

    private static final int DEFAULT_PAGE_SIZE = 30;
    private static final int MAX_PAGE_SIZE = 100;

    private final MessageRepository messageRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final Function<String, UUID> emailToUserId; // voir câblage ci-dessous

    public MessageHistoryController(MessageRepository messageRepository,
                                    RoomMemberRepository roomMemberRepository,
                                    Function<String, UUID> emailToUserId) {
        this.messageRepository = messageRepository;
        this.roomMemberRepository = roomMemberRepository;
        this.emailToUserId = emailToUserId;
    }

    @GetMapping
    public List<MessageDto> history(@PathVariable UUID roomId,
                                    @RequestParam(required = false) Instant before,
                                    @RequestParam(required = false) Integer limit,
                                    Principal principal) {
        UUID userId = emailToUserId.apply(principal.getName());

        if (!roomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Salon introuvable");
        }

        Instant cursor = before != null ? before : Instant.now();
        PageRequest pageRequest = PageRequest.of(0, resolvePageSize(limit));

        return messageRepository.findHistory(roomId, cursor, pageRequest).stream()
                .map(this::toDto)
                .toList();
    }

    private int resolvePageSize(Integer requested) {
        if (requested == null) return DEFAULT_PAGE_SIZE;
        return Math.min(Math.max(requested, 1), MAX_PAGE_SIZE);
    }

    private MessageDto toDto(Message message) {
        return new MessageDto(message.getId(), message.getRoomId(), message.getSenderId(),
                message.getContent(), message.getCreatedAt());
    }
}