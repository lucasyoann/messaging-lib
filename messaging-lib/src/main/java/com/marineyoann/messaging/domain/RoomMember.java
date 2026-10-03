package com.marineyoann.messaging.domain;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "room_member")
@IdClass(RoomMember.RoomMemberId.class)
public class RoomMember {

    @Id
    @Column(name = "room_id")
    private UUID roomId;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomMemberRole role;

    @Column(nullable = false)
    private Instant joinedAt;

    protected RoomMember() {
    }

    private RoomMember(UUID roomId, UUID userId, RoomMemberRole role, Instant joinedAt) {
        this.roomId = roomId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public static RoomMember owner(UUID roomId, UUID userId) {
        return new RoomMember(roomId, userId, RoomMemberRole.OWNER, Instant.now());
    }

    public static RoomMember member(UUID roomId, UUID userId) {
        return new RoomMember(roomId, userId, RoomMemberRole.MEMBER, Instant.now());
    }

    public UUID getRoomId() {
        return roomId;
    }

    public UUID getUserId() {
        return userId;
    }

    public RoomMemberRole getRole() {
        return role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    /** Clé composite (room_id, user_id) requise par @IdClass. */
    public static class RoomMemberId implements Serializable {
        private UUID roomId;
        private UUID userId;

        public RoomMemberId() {
        }

        public RoomMemberId(UUID roomId, UUID userId) {
            this.roomId = roomId;
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RoomMemberId that)) return false;
            return Objects.equals(roomId, that.roomId) && Objects.equals(userId, that.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(roomId, userId);
        }
    }
}
