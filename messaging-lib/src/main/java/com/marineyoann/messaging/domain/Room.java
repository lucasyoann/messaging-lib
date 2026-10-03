package com.marineyoann.messaging.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "room")
public class Room {

    @Id
    private UUID id;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomType type;

    @Column(nullable = false)
    private Instant createdAt;

    protected Room() {
        // requis par JPA
    }

    private Room(UUID id, String name, RoomType type, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.createdAt = createdAt;
    }

    public static Room newGroup(String name) {
        return new Room(UUID.randomUUID(), name, RoomType.GROUP, Instant.now());
    }

    public static Room newDirect() {
        return new Room(UUID.randomUUID(), null, RoomType.DIRECT, Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void rename(String newName) {
        if (type != RoomType.GROUP) {
            throw new IllegalStateException("Seuls les salons de type GROUP peuvent être renommés");
        }
        this.name = newName;
    }

    public RoomType getType() {
        return type;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Destination STOMP dédiée à ce salon, ex: /topic/room.<id> */
    public String stompDestination() {
        return "/topic/room." + id;
    }
}
