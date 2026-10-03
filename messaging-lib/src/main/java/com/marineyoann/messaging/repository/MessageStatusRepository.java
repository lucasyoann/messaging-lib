package com.marineyoann.messaging.repository;

import com.marineyoann.messaging.domain.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageStatusRepository extends JpaRepository<MessageStatus, MessageStatus.MessageStatusId> {

    List<MessageStatus> findByMessageId(UUID messageId);

    Optional<MessageStatus> findByMessageIdAndUserId(UUID messageId, UUID userId);
}
