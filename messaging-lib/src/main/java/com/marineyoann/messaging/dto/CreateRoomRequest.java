package com.marineyoann.messaging.dto;

import com.marineyoann.messaging.domain.RoomType;

import java.util.List;
import java.util.UUID;

public record CreateRoomRequest(RoomType type, String name, List<UUID> memberIds) {
}