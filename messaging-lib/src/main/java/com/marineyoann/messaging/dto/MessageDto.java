package com.marineyoann.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageDto(
        UUID id,
        UUID roomId,
        UUID senderId,
        String content,
        Instant createdAt
) {
}