package com.marineyoann.messaging.repository;

import com.marineyoann.messaging.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("""
           select m from Message m
           where m.roomId = :roomId and m.createdAt < :before
           order by m.createdAt desc
           """)
    List<Message> findHistory(UUID roomId, Instant before, Pageable pageable);
}
