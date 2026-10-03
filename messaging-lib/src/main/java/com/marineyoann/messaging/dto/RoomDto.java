package com.marineyoann.messaging.dto;

import com.marineyoann.messaging.domain.RoomType;

import java.time.Instant;
import java.util.UUID;

public record RoomDto(UUID id, String name, RoomType type, Instant createdAt) {
}