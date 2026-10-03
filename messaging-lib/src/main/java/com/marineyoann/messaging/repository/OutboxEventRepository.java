package com.marineyoann.messaging.repository;

import com.marineyoann.messaging.domain.OutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query("""
            select e from OutboxEvent e
            where e.publishedAt is null and e.attempts < 10
            order by e.createdAt asc
            """)
    List<OutboxEvent> findBatchToPublish(Pageable pageable);
}
