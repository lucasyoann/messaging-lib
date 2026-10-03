package com.marineyoann.messaging.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageCreatedEvent(UUID messageId,
                                  UUID roomId,
                                  UUID senderId,
                                  String content,
                                  Instant createdAt,
                                  List<UUID> recipientIds) {
}
